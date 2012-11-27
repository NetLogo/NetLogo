// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.compiler.CompilerExceptionThrowers.{ cAssert, exception }
import org.nlogo.api.{ CompilerException, Let, Program, Token, TokenType }
import org.nlogo.nvm.{ Instruction, Procedure, Reporter }
import org.nlogo.prim._

/**
 * Converts identifier tokens into instances of primitives.  In "forgiving" mode, used by
 * AutoConverter, unknown identifiers are assumed to be references to global variables that the
 * compiler doesn't know about yet. - ST 7/7/06 */
private class IdentifierParser(program: Program,
                               oldProcedures: Compiler.ProceduresMap,
                               newProcedures: Compiler.ProceduresMap,
                               forgiving: Boolean) {
  def process(tokens: Iterator[Token], procedure: Procedure): Seq[Token] = {
    // make sure the procedure name doesn't conflict with a special identifier -- CLB
    checkProcedureName(procedure)
    val it = new CountedIterator(tokens)
    def processToken(token: Token): Token = {
      if(token.tpe == TokenType.IDENT || token.tpe == TokenType.VARIABLE)
        processToken2(token,procedure, it.count)
      else token
    }
    it.map(processToken).toSeq
  }
  private def getLetFromArg(p: Procedure, ident: String, tokPos: Int): Option[Let] = {
    def checkLet(let: Let): Option[Let] =
      if(tokPos < let.start || tokPos > let.end || let.name != ident)
        None
      else
        Some(let)
    import collection.JavaConverters._
    p.lets.map(checkLet).find(_.isDefined).getOrElse(None)
  }
  private def processToken2(tok: Token, procedure: Procedure, tokPos: Int): Token = {
    val ident = tok.value.asInstanceOf[String]
    if(ident.startsWith("?")) {
      val varNumber =
        // default number is 1 (i.e., if they just use "?")
        if(ident.length == 1) 1
        // if it's more than just "?", it needs to be an integer.
        else
          try Integer.parseInt(ident.substring(1))
          catch { case e: NumberFormatException =>
            exception(INVALID_TASK_VARIABLE, tok) }
      cAssert(varNumber > 0, INVALID_TASK_VARIABLE, tok)
      newToken(new _taskvariable(varNumber),
               ident, TokenType.REPORTER, tok.startPos, tok.endPos, tok.fileName)
    }
    // kludgy to special case this, but we only have one such prim,
    // so oh well... - ST 7/8/06
    else if(ident == "RANDOM-OR-RANDOM-FLOAT")
      exception(RANDOM_OR_RANDOM_FLOAT_ERROR, tok)
    else if(getLetFromArg(procedure, ident, tokPos).isDefined)
      newToken(new _letvariable(getLetFromArg(procedure, ident, tokPos).get, ident),
               ident, TokenType.REPORTER, tok.startPos, tok.endPos, tok.fileName)
    else if(procedure.args.contains(ident))
      newToken(new _procedurevariable(procedure.args.indexOf(ident), ident),
               ident, TokenType.REPORTER, tok.startPos, tok.endPos, tok.fileName)
    else {
      // go thru our identifierHandlers, if one triggers, return the result
      BreedIdentifierHandler.process(tok, program).getOrElse{
        val callproc =
          oldProcedures.getOrElse(ident,
            newProcedures.getOrElse(ident,
              return newToken(getAgentVariableReporter(ident, tok),
                              ident, TokenType.REPORTER, tok.startPos, tok.endPos, tok.fileName)))
        val (tokenType, caller) =
          if (callproc.isReporter)
            (TokenType.REPORTER, new _callreport(callproc))
          else
            (TokenType.COMMAND, new _call(callproc))
        newToken(caller, ident, tokenType, tok.startPos, tok.endPos, tok.fileName)
      }
    }
  }

  private def getAgentVariableReporter(varName: String, tok: Token): Reporter = {
    if(program.turtlesOwn.contains(varName) && program.linksOwn.contains(varName))
      new _turtleorlinkvariable(varName)
    else if(program.turtlesOwn.contains(varName))
      new _turtlevariable(program.turtlesOwn.indexOf(varName))
    else if(program.patchesOwn.contains(varName))
      new _patchvariable(program.patchesOwn.indexOf(varName))
    else if(program.linksOwn.contains(varName))
      new _linkvariable(program.linksOwn.indexOf(varName))
    else if(program.globals.contains(varName))
      new _observervariable(program.globals.indexOf(varName))
    else if(program.breeds.values.exists(_.owns.contains(varName)))
      new _breedvariable(varName)
    else if(program.linkBreeds.values.exists(_.owns.contains(varName)))
      new _linkbreedvariable(varName)
    else if(forgiving)
      new _unknownidentifier
    else
      exception("Nothing named " + varName + " has been defined",
                new Token(varName, tok.tpe, tok.value)
                         (tok.startPos, tok.startPos + varName.length, tok.fileName))
  }
  private def checkProcedureName(procedure: Procedure) {
    val newVal: AnyRef =
      // if the proc name doesn't trigger any identifier rules it's treated as a variable reference,
      // and if there's no variable with that name, CompilerException is raised -- CLB
      try { processToken2(procedure.nameToken, procedure, 0).value }
      catch { case ex: CompilerException => return }
    cAssert( ( newVal.isInstanceOf[_call]
              || newVal.isInstanceOf[_callreport]
              || newVal.isInstanceOf[_unknownidentifier]),
            "Cannot use " + procedure.name + " as a procedure name.  Conflicts with: " + newVal,
            procedure.nameToken)
  }
  private def newToken(instr: Instruction, name: String, tpe: TokenType, startPos: Int, endPos: Int, fileName: String) = {
    val tok = new Token(name, tpe, instr)(startPos, endPos, fileName)
    instr.token(tok)
    tok
  }
  /// error texts
  private val INVALID_TASK_VARIABLE =
    "variables may not begin with a question mark unless they are the special variables ?, ?1, ?2, ..."
  private val RANDOM_OR_RANDOM_FLOAT_ERROR =
    "This code was written for an old version of NetLogo in which the RANDOM primitive sometimes reported " +
    "an integer (e.g. 4), other times a floating point number (e.g. 4.326), depending on its input. " +
    "That's no longer true in this version; instead, we now have two separate primitives. So you must " +
    "replace this with either RANDOM or RANDOM-FLOAT depending on whether you want an integer or " +
    "a floating point result."
}
