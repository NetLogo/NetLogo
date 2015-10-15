// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
  core.{ExtensionManager, Fail, FrontEndInterface,
    Primitive, PrimitiveCommand, PrimitiveReporter, Program, Token, TokenType},
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
  def lookup(token: Token, fn: String => Option[core.TokenHolder], newType: TokenType): Option[(TokenType, core.Instruction)] =
    fn(token.value.asInstanceOf[String]).map{holder =>
      (newType, holder.asInstanceOf[core.Instruction])}
}

object CommandHandler extends PrimitiveHandler {
  override def apply(token: Token) =
    lookup(token, FrontEnd.tokenMapper.getCommand  _, TokenType.Command)
}

object ReporterHandler extends PrimitiveHandler {
  override def apply(token: Token) =
    lookup(token, FrontEnd.tokenMapper.getReporter  _, TokenType.Reporter)
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
  override def apply(token: Token) =
    getAgentVariableReporter(token.value.asInstanceOf[String])
      .map{(TokenType.Reporter, _)}
  import PartialFunction.condOpt
  def boolOpt[T](b: Boolean)(x: => T) =
    if (b) Some(x) else None
  def getAgentVariableReporter(varName: String): Option[core.Reporter] =
    boolOpt(program.breeds.values.exists(_.owns.contains(varName)))(
      new core.prim._breedvariable(varName)) orElse
    boolOpt(program.linkBreeds.values.exists(_.owns.contains(varName)))(
      new core.prim._linkbreedvariable(varName)) orElse
    boolOpt(program.turtlesOwn.contains(varName) && program.linksOwn.contains(varName))(
      new core.prim._turtleorlinkvariable(varName)) orElse
    condOpt(program.turtlesOwn.indexOf(varName)) {
      case n if n != -1 => new core.prim._turtlevariable(n) } orElse
    condOpt(program.patchesOwn.indexOf(varName)) {
      case n if n != -1 => new core.prim._patchvariable(n) } orElse
    condOpt(program.linksOwn.indexOf(varName)) {
      case n if n != -1 => new core.prim._linkvariable(n) } orElse
    condOpt(program.globals.indexOf(varName)) {
      case n if n != -1 => new core.prim._observervariable(n) }
}
