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
      new ExtensionPrimitiveHandler(extensionManager)(token)
        .orElse(CommandHandler(token))
        .orElse(ReporterHandler(token))
        .orElse(TaskVariableHandler(token))
        .orElse(new LetVariableHandler(lets, it.count)(token))
        .orElse(new ProcedureVariableHandler(procedure.args)(token))
        .orElse(new BreedHandler(program)(token))
        .orElse(new CallHandler(procedures)(token))
        .orElse(new AgentVariableReporterHandler(program)(token))
        .getOrElse(fail(token))
    checkProcedureName(procedure)
    it.map{token => token.tpe match {
      case TokenType.Ident => processOne(token)
      case _ => token
    }}
  }

  /// errors

  def fail(token: Token): Nothing =
    exception(unknownIdentifier(
      token.value.asInstanceOf[String]), token)

  private def unknownIdentifier(s: String) =
    "Nothing named " + s + " has been defined"
  private def invalidProcedureName(theirs: String, ours: String) =
    "Cannot use " + theirs + " as a procedure name.  Conflicts with: " + ours

}
