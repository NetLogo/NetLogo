// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
  core.{ AgentVariableSet, Command, ExtensionManager, Fail, FrontEndInterface, Instruction,
    Primitive, PrimitiveCommand, PrimitiveReporter, Program, Reporter,
    Token, TokenMapperInterface, TokenType},
    FrontEndInterface.ProceduresMap,
    Fail._

trait NameHandler extends (Token => Option[(TokenType, core.Instruction)])

class ProcedureVariableHandler(args: Seq[String])
extends NameHandler {
  override def apply(token: Token) =
    Some(token.value.asInstanceOf[String])
      .filter(args.contains)
      .map(ident =>
        (TokenType.Reporter, new core.prim._procedurevariable(args.indexOf(ident), ident)))
}

class CallHandler(procedures: ProceduresMap) extends NameHandler {
  override def apply(token: Token) = {
    val name = token.value.asInstanceOf[String]
    Some(name)
      .flatMap{procedures.get}
      .map{callproc =>
        if (callproc.isReporter)
          (TokenType.Reporter, new core.prim._callreport(callproc))
        else
          (TokenType.Command, new core.prim._call(callproc))}
  }
}

abstract class PrimitiveHandler extends NameHandler {
  type InstructionType <: Instruction
  def lookup(token: Token, fn: String => Option[InstructionType], newType: TokenType): Option[(TokenType, core.Instruction)] =
    fn(token.value.asInstanceOf[String]).map{holder =>
      (newType, holder)}
}

class CommandHandler(tokenMapper: TokenMapperInterface) extends PrimitiveHandler {
  type InstructionType = Command
  override def apply(token: Token) =
    lookup(token, tokenMapper.getCommand  _, TokenType.Command)
}

class ReporterHandler(tokenMapper: TokenMapperInterface) extends PrimitiveHandler {
  type InstructionType = Reporter
  override def apply(token: Token) =
    lookup(token, tokenMapper.getReporter  _, TokenType.Reporter)
}

// replaces an identifier token with its imported implementation, if necessary
class ExtensionPrimitiveHandler(extensionManager: ExtensionManager) extends NameHandler {
  override def apply(token: Token) =
    if(token.tpe != TokenType.Ident ||
       extensionManager == null || !extensionManager.anyExtensionsLoaded)
      None
    else {
      val name = token.value.asInstanceOf[String]
      val replacement = extensionManager.replaceIdentifier(name)
      replacement match {
        // if there's no replacement, make no change.
        case null =>
          None
        case primitive =>
          val newType =
            if(primitive.isInstanceOf[PrimitiveCommand])
              TokenType.Command
            else TokenType.Reporter
          Some((newType, wrap(primitive, name)))
      }
    }
  private def wrap(primitive: Primitive, name: String): core.Instruction =
    primitive match {
      case c: PrimitiveCommand  =>
        new core.prim._extern(c.getSyntax)
      case r: PrimitiveReporter =>
        new core.prim._externreport(r.getSyntax)
    }
}

// default number is 1 (i.e., if they just use "?")
// if it's more than just "?", it needs to be an integer.
object TaskVariableHandler extends NameHandler {
  override def apply(token: Token) =
    Some(token.value.asInstanceOf[String])
      .filter(_.startsWith("?"))
      .map{ident =>
        val varNumber =
          if(ident.length == 1)
            1
          else
            try Integer.parseInt(ident.substring(1))
            catch { case e: NumberFormatException =>
              exception(InvalidTaskVariable, token) }
        cAssert(varNumber > 0, InvalidTaskVariable, token)
        (TokenType.Reporter, new core.prim._taskvariable(varNumber))
    }
  val InvalidTaskVariable =
    "variables may not begin with a question mark unless they are the special variables ?, ?1, ?2, ..."
}

class AgentVariableReporterHandler(program: Program) extends NameHandler {
  import scala.collection.immutable.ListMap
  import PartialFunction.condOpt
  override def apply(token: Token) =
    getAgentVariableReporter(token.value.asInstanceOf[String])
      .map{(TokenType.Reporter, _)}
  def boolOpt[T](b: Boolean)(x: => T) =
    if (b) Some(x) else None
  def mapIndexOpt[T, S](map: ListMap[String, T], key: String)(f: (Int, T) => S): Option[S] =
    if (map.contains(key)) {
      val index = map.keys.toSeq.indexOf(key)
      Some(f(index, map(key)))
    } else None
  def getAgentVariableReporter(varName: String): Option[core.Reporter] =
    boolOpt(program.breeds.values.exists(_.owns.contains(varName)))(
      new core.prim._breedvariable(varName)) orElse
    boolOpt(program.linkBreeds.values.exists(_.owns.contains(varName)))(
      new core.prim._linkbreedvariable(varName)) orElse
    boolOpt(program.turtlesOwn.contains(varName) && program.linksOwn.contains(varName))(
      new core.prim._turtleorlinkvariable(varName,
        program.turtleVars(varName) | program.linkVars(varName))) orElse
    mapIndexOpt(program.turtleVars, varName) { (i, tpe) =>
      new core.prim._turtlevariable(i, tpe) } orElse
    mapIndexOpt(program.patchVars, varName) { (i, tpe) =>
      new core.prim._patchvariable(i, tpe) } orElse
    mapIndexOpt(program.linkVars, varName) { (i, tpe) =>
      new core.prim._linkvariable(i, tpe) } orElse
    mapIndexOpt(program.observerVars, varName) { (i, tpe) =>
      new core.prim._observervariable(i, tpe) } orElse
    condOpt(program.globals.indexOf(varName)) {
      case n if n != -1 => new core.prim._observervariable(n) }
}

// this should only be used for colorization, when we may or may not have
// a complete program
class BuiltInAgentVariableReporterHandler(agentVariables: AgentVariableSet)
  extends NameHandler {
    val variableMap = Map[Seq[String], Int => core.Reporter](
      agentVariables.implicitObserverVariableTypeMap.keys.toSeq -> (i => new core.prim._observervariable(i)),
      agentVariables.implicitTurtleVariableTypeMap.keys.toSeq   -> (i => new core.prim._turtlevariable(i)),
      agentVariables.implicitPatchVariableTypeMap.keys.toSeq    -> (i => new core.prim._patchvariable(i)),
      agentVariables.implicitLinkVariableTypeMap.keys.toSeq     -> (i => new core.prim._linkvariable(i)))

  override def apply(token: Token) =
    variableMap.find {
      case (vars, _) => vars.contains(token.value.asInstanceOf[String])
    }.map {
      case (vars, newReporter) => (TokenType.Reporter -> newReporter(vars.indexOf(token.value.asInstanceOf[String])))
    }
}
