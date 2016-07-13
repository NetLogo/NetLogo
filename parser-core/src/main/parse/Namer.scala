// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
  core.{ Dialect, DummyExtensionManager, ExtensionManager, FrontEndInterface, FrontEndProcedure, Instruction,
  Program, Token, TokenMapperInterface, TokenType },
  core.Fail._

/**
  * Classifies identifier tokens as commands or reporters.
  *
  * This is basically just a function from Iterator[Token] to Iterator[Token].
  * Most tokens pass through unchanged, but each token of type Ident is
  * replaced with a new token of type TokenType.Command or TokenType.Reporter.
  *
  * One additional check is performed: checkProcedureName makes sure the name (and input names) of
  * each procedure aren't also the name of anything else.  (That check happens here because here is
  * where the knowledge of what names are taken resides.)
  */
class Namer(
  program: Program,
  procedures: FrontEndInterface.ProceduresMap,
  procedure: FrontEndProcedure,
  extensionManager: ExtensionManager) extends TokenTransformer[Unit] {

  // the handlers are mutually exclusive (only one applies), so the order the handlers
  // appear is arbitrary, except that for checkName to work, ProcedureVariableHandler
  // and CallHandler must come last - ST 5/14/13, 5/16/13
  lazy val handlers = Seq[Token => Option[(TokenType, core.Instruction)]](
    new CommandHandler(program.dialect.tokenMapper),
    new ReporterHandler(program.dialect.tokenMapper),
    TaskVariableHandler,
    new BreedHandler(program),
    new AgentVariableReporterHandler(program),
    new ExtensionPrimitiveHandler(extensionManager),
    new ProcedureVariableHandler(procedure.args),
    new CallHandler(procedures))

  def validateProcedure(): Unit = {
    for (token <- procedure.nameToken +: procedure.argTokens)
      checkName(token)
  }

  override def initialState: Unit = ()

  override def transform(t: Token, state: Unit): (Token, Unit) = {
    t.tpe match {
      case TokenType.Ident =>
        (processOne(t).getOrElse(
          t.refine(core.prim._unknownidentifier(), tpe = TokenType.Reporter)), ())
      case _ => (t, ())
    }
  }

  private def checkName(token: Token) {
    val newVal = processOne(token).map(_.value).get
    val ok = newVal.isInstanceOf[core.prim._call] ||
    newVal.isInstanceOf[core.prim._callreport] ||
    newVal.isInstanceOf[core.prim._procedurevariable]
    cAssert(ok, alreadyTaken(userFriendlyName(newVal), token.text.toUpperCase), token)
  }

  private def processOne(token: Token): Option[Token] = {
    handlers.flatMap(_(token))
      .headOption
      .map{case (tpe, instr) =>
        val newToken = token.copy(tpe = tpe, value = instr)
        instr.token = newToken
        newToken
      }
  }

  private def alreadyTaken(theirs: String, ours: String) =
    "There is already " + theirs + " called " + ours

  private def userFriendlyName(a: AnyRef): String =
    a match {
      case _: core.prim._extern       => "an extension command"
      case _: core.prim._externreport => "an extension reporter"
      case _                          => s"a ${a.getClass.getSimpleName}"
    }

}

object Namer {

  // provides token type information for commands, reporters, keywords, and constants
  def basicNamer(dialect: Dialect, extensionManager: ExtensionManager): Token => Token = {
    def makeToken(f: Token => Option[(TokenType, AnyRef)])(tok: Token): Token =
      if (tok.tpe == TokenType.Ident)
        f(tok) match {
          case Some((tpe, v: Instruction)) => tok.refine(v, tpe = tpe)
          case Some((tpe, v: AnyRef)) => tok.copy(tpe = tpe, value = v)
          case None                   => tok
        }
      else
        tok

    (Namer0.nameKeywordsAndConstants _)             andThen
    (makeToken(new ReporterHandler(dialect.tokenMapper)) _) andThen
    (makeToken(new CommandHandler(dialect.tokenMapper)) _)  andThen
    (makeToken(new ExtensionPrimitiveHandler(extensionManager)) _)  andThen
    (makeToken(new BuiltInAgentVariableReporterHandler(dialect.agentVariables)) _)  andThen
    ((t: Token) =>
        if (t.tpe == TokenType.Reporter && t.value.isInstanceOf[core.prim._symbol])
          t.copy(tpe = TokenType.Ident, value = t.value)
        else
          t)
  }
}
