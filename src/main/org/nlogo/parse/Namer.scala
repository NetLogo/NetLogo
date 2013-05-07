// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.{ api, nvm, parse0, prim },
  api.{ Token, TokenType },
  Fail._

/**
  * Classifies identifier tokens as commands or reporters.
  *
  * This is basically just a function from Iterator[Token] to Iterator[Token].
  * Most tokens pass through unchanged, but each token of type Ident is
  * replaced with a new token of type TokenType.Command or TokenType.Reporter,
  * with an instance of nvm.Command or nvm.Reporter stored in its value slot.
  *
  * One additional check is performed: checkProcedureName makes sure the
  * name of each procedure isn't also the name of anything else.  (That check
  * happens here because here is where the knowledge of what names are taken
  * resides.)
  */
class Namer(
  program: api.Program,
  procedures: nvm.ParserInterface.ProceduresMap,
  extensionManager: api.ExtensionManager,
  lets: Vector[api.Let]) {

  def process(tokens: Iterator[Token], procedure: nvm.Procedure): Iterator[Token] = {
    val it = new parse0.CountedIterator(tokens)
    def checkProcedureName(procedure: nvm.Procedure) {
      val newVal: AnyRef =
        // if the proc name doesn't trigger any identifier rules it's treated as a variable reference,
        // and if there's no variable with that name, CompilerException is raised -- CLB
        try processOne(procedure.nameToken).value
        catch { case ex: api.CompilerException => return }
      val ok = newVal.isInstanceOf[prim._call] ||
        newVal.isInstanceOf[prim._callreport]
      cAssert(ok, invalidProcedureName(procedure.name, newVal.toString), procedure.nameToken)
    }
    // the handlers are mutually exclusive (only one applies), so the order the handlers
    // appear is arbitrary - ST 5/14/13
    def processOne(token: Token): Token =
      ExtensionPrimitiveHandler(token)
        .orElse(CommandHandler(token))
        .orElse(ReporterHandler(token))
        .orElse(TaskVariableHandler(token))
        .orElse(new LetVariableHandler(it.count)(token))
        .orElse(new ProcedureVariableHandler(procedure.args)(token))
        .orElse(BreedHandler(token))
        .orElse(CallHandler(token))
        .orElse(AgentVariableReporterHandler(token))
        .getOrElse(fail(token))
    checkProcedureName(procedure)
    it.map{token => token.tpe match {
      case TokenType.Ident => processOne(token)
      case _ => token
    }}
  }

  abstract class NameHandler {
    def apply(token: Token): Option[Token] =
      handle(token).map{case (tpe, instr) =>
        val newToken = token.copy(tpe = tpe, value = instr)
        instr.token(newToken)
        newToken
      }
    def handle(token: Token): Option[(TokenType, nvm.Instruction)]
  }

  class ProcedureVariableHandler(args: Seq[String])
  extends NameHandler {
    override def handle(token: Token) =
      Some(token.value.asInstanceOf[String])
        .filter(args.contains)
        .map(ident =>
          (TokenType.Reporter, new prim._procedurevariable(args.indexOf(ident), ident)))
  }

  // kludgy to special case this, but we only have one such prim,
  // so oh well... - ST 7/8/06
  class LetVariableHandler(count: Int)
  extends NameHandler {
    override def handle(token: Token) = {
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
          getLetFromArg(ident, count).map(let =>
            (TokenType.Reporter, new prim._letvariable(let)))}
    }
  }

  object CallHandler extends NameHandler {
    override def handle(token: Token) =
      Some(token.value.asInstanceOf[String])
        .flatMap{procedures.get}
        .map{callproc =>
          if (callproc.isReporter)
            (TokenType.Reporter, new prim._callreport(callproc))
          else
            (TokenType.Command, new prim._call(callproc))}
  }

  private def lookup(token: Token, fn: String => Option[api.TokenHolder], newType: TokenType): Option[(TokenType, nvm.Instruction)] =
    fn(token.value.asInstanceOf[String]).map{holder =>
      (newType, holder.asInstanceOf[nvm.Instruction])}

  object CommandHandler extends NameHandler {
    override def handle(token: Token) =
      lookup(token, Parser.tokenMapper.getCommand  _, TokenType.Command)
  }

  object ReporterHandler extends NameHandler {
    override def handle(token: Token) =
      lookup(token, Parser.tokenMapper.getReporter  _, TokenType.Reporter)
  }

  // go thru our breed prim handlers, if one triggers, return the result
  object BreedHandler extends NameHandler {
    override def handle(token: Token) =
      parse0.BreedIdentifierHandler.process(token, program) map {
        case (className, breedName, tokenType) =>
          (tokenType, Instantiator.newInstance[nvm.Instruction](
            Class.forName("org.nlogo.prim." + className), breedName))
      }
  }

  // replaces an identifier token with its imported implementation, if necessary
  object ExtensionPrimitiveHandler extends NameHandler {
    override def handle(token: Token) =
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
    override def handle(token: Token) =
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
  }

  object AgentVariableReporterHandler extends NameHandler {
    override def handle(token: Token) =
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

  /// errors

  def fail(token: Token): Nothing =
    exception(unknownIdentifier(
      token.value.asInstanceOf[String]), token)

  private def unknownIdentifier(s: String) =
    "Nothing named " + s + " has been defined"
  private def invalidProcedureName(theirs: String, ours: String) =
    "Cannot use " + theirs + " as a procedure name.  Conflicts with: " + ours
  private val InvalidTaskVariable =
    "variables may not begin with a question mark unless they are the special variables ?, ?1, ?2, ..."

}
