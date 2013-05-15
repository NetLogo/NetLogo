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
    // the handlers are mutually exclusive (only one applies), so the order the handlers
    // appear is arbitrary, except that for checkProcedureName to work, CallHandler must
    // be last - ST 5/14/13
    val handlers = Stream[Token => Option[Token]](
      CommandHandler,
      ReporterHandler,
      TaskVariableHandler,
      new LetVariableHandler(lets, () => it.count),
      new ProcedureVariableHandler(procedure.args),
      new BreedHandler(program),
      new AgentVariableReporterHandler(program),
      new ExtensionPrimitiveHandler(extensionManager),
      new CallHandler(procedures))
    def processOne(token: Token): Option[Token] =
      handlers.flatMap(_(token)).headOption
    def checkProcedureName(procedure: nvm.Procedure) {
      val newVal = processOne(procedure.nameToken).map(_.value).get
      val ok = newVal.isInstanceOf[prim._call] ||
        newVal.isInstanceOf[prim._callreport]
      cAssert(ok, invalidProcedureName(procedure.name, newVal.toString), procedure.nameToken)
    }
    checkProcedureName(procedure)
    it.map{token => token.tpe match {
      case TokenType.Ident =>
        processOne(token).getOrElse(fail(token))
      case _ =>
        token
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
