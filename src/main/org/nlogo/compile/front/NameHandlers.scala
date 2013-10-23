// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package front

import org.nlogo.{ api, nvm, parse, prim },
  api.{ Token, TokenType },
  Fail._

trait NameHandler extends (Token => Option[(TokenType, nvm.Instruction)])

class ProcedureVariableHandler(args: Seq[String])
extends NameHandler {
  override def apply(token: Token) =
    Some(token.value.asInstanceOf[String])
      .filter(args.contains)
      .map(ident =>
        (TokenType.Reporter, new prim._procedurevariable(args.indexOf(ident), ident)))
}

class LetVariableHandler(lets: Vector[api.Let], count: () => Int)
extends NameHandler {
  override def apply(token: Token) = {
    def getLetFromArg(ident: String, tokPos: Int): Option[api.Let] = {
      def checkLet(let: api.Let): Option[api.Let] =
        if(tokPos < let.start || tokPos > let.end || let.name != ident)
          None
        else
          Some(let)
      lets.map(checkLet).find(_.isDefined).getOrElse(None)
    }
    Some(token.value.asInstanceOf[String])
      .flatMap{ident =>
        getLetFromArg(ident, count()).map(let =>
          (TokenType.Reporter, new prim._letvariable(let)))}
  }
}

class CallHandler(procedures: nvm.FrontEndInterface.ProceduresMap) extends NameHandler {
  override def apply(token: Token) =
    Some(token.value.asInstanceOf[String])
      .flatMap{procedures.get}
      .map{callproc =>
        if (callproc.isReporter)
          (TokenType.Reporter, new prim._callreport(callproc))
        else
          (TokenType.Command, new prim._call(callproc))}
}

abstract class PrimitiveHandler extends NameHandler {
  def lookup(token: Token, fn: String => Option[api.TokenHolder], newType: TokenType): Option[(TokenType, nvm.Instruction)] =
    fn(token.value.asInstanceOf[String]).map{holder =>
      (newType, holder.asInstanceOf[nvm.Instruction])}
}

object CommandHandler extends PrimitiveHandler {
  override def apply(token: Token) =
    lookup(token, FrontEnd.tokenMapper.getCommand  _, TokenType.Command)
}

object ReporterHandler extends PrimitiveHandler {
  override def apply(token: Token) =
    lookup(token, FrontEnd.tokenMapper.getReporter  _, TokenType.Reporter)
}

// go thru our breed prim handlers, if one triggers, return the result
class BreedHandler(program: api.Program) extends NameHandler {
  override def apply(token: Token) =
    parse.BreedIdentifierHandler.process(token, program) map {
      case (className, breedName, tokenType) =>
        (tokenType, Instantiator.newInstance[nvm.Instruction](
          Class.forName("org.nlogo.prim." + className), breedName))
    }
}

// replaces an identifier token with its imported implementation, if necessary
class ExtensionPrimitiveHandler(extensionManager: api.ExtensionManager) extends NameHandler {
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
            if(primitive.isInstanceOf[api.Command])
              TokenType.Command
            else TokenType.Reporter
          Some((newType, wrap(primitive, name)))
      }
    }
  private def wrap(primitive: api.Primitive, name: String): nvm.Instruction =
    primitive match {
      case c: api.Command  =>
        new prim._extern(c)
      case r: api.Reporter =>
        new prim._externreport(r)
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
        (TokenType.Reporter, new prim._taskvariable(varNumber))
    }
  val InvalidTaskVariable =
    "variables may not begin with a question mark unless they are the special variables ?, ?1, ?2, ..."
}

class AgentVariableReporterHandler(program: api.Program) extends NameHandler {
  override def apply(token: Token) =
    getAgentVariableReporter(token.value.asInstanceOf[String])
      .map{(TokenType.Reporter, _)}
  import PartialFunction.condOpt
  def boolOpt[T](b: Boolean)(x: => T) =
    if (b) Some(x) else None
  def getAgentVariableReporter(varName: String): Option[nvm.Reporter] =
    boolOpt(program.breeds.values.exists(_.owns.contains(varName)))(
      new prim._breedvariable(varName)) orElse
    boolOpt(program.linkBreeds.values.exists(_.owns.contains(varName)))(
      new prim._linkbreedvariable(varName)) orElse
    boolOpt(program.turtlesOwn.contains(varName) && program.linksOwn.contains(varName))(
      new prim._turtleorlinkvariable(varName)) orElse
    condOpt(program.turtlesOwn.indexOf(varName)) {
      case n if n != -1 => new prim._turtlevariable(n) } orElse
    condOpt(program.patchesOwn.indexOf(varName)) {
      case n if n != -1 => new prim._patchvariable(n) } orElse
    condOpt(program.linksOwn.indexOf(varName)) {
      case n if n != -1 => new prim._linkvariable(n) } orElse
    condOpt(program.globals.indexOf(varName)) {
      case n if n != -1 => new prim._observervariable(n) }
}
