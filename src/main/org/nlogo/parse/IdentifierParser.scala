// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import Fail.{ cAssert, exception }
import org.nlogo.{ api, nvm, prim }
import api.{ CompilerException, Let, Program, Token, TokenType }
import nvm.{ Instruction, Procedure, Reporter }
import nvm.CompilerInterface.ProceduresMap

/**
  * Converts identifier tokens into instances of primitives.
  * In "forgiving" mode, used by
  * AutoConverter, unknown identifiers are assumed to be references to global variables that the
  * compiler doesn't know about yet.
  */

class IdentifierParser(
  program: Program,
  oldProcedures: ProceduresMap,
  newProcedures: ProceduresMap,
  extensionManager: api.ExtensionManager,
  forgiving: Boolean = false) {

  def process(tokens: Iterator[Token], procedure: Procedure): Seq[Token] = {
    // make sure the procedure name doesn't conflict with a special identifier -- CLB
    checkProcedureName(procedure)
    val it = new CountedIterator(tokens)
    def processToken(token: Token): Token = {
      if(token.tpe == TokenType.IDENT || token.tpe == TokenType.VARIABLE)
        processToken2(token, procedure, it.count)
      else token
    }
    it.map(processTokenWithExtensionManager).map(processToken).toSeq
  }

  // replaces an identifier token with its imported implementation, if necessary
  private def processTokenWithExtensionManager(token: Token): Token = {
    def wrap(primitive: api.Primitive, name: String): nvm.Instruction =
      primitive match {
        case c: api.Command  =>
          new prim._extern(c)
        case r: api.Reporter =>
          new prim._externreport(r)
      }
    if(token.tpe != TokenType.IDENT ||
       extensionManager == null || !extensionManager.anyExtensionsLoaded)
      token
    else {
      val name = token.value.asInstanceOf[String]
      val replacement = extensionManager.replaceIdentifier(name)
      replacement match {
        // if there's no replacement, make no change.
        case null =>
          token
        case primitive =>
          val newType =
            if(primitive.isInstanceOf[api.Command])
              TokenType.COMMAND
            else TokenType.REPORTER
          val instruction = wrap(primitive, name)
          val newToken = Token(token.name, newType, instruction)(
            token.startPos, token.endPos, token.fileName)
          instruction.token(newToken)
          newToken
      }
    }
  }

  private def getLetFromArg(p: Procedure, ident: String, tokPos: Int): Option[Let] = {
    def checkLet(let: Let): Option[Let] =
      if(tokPos < let.start || tokPos > let.end || let.name != ident)
        None
      else
        Some(let)
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
            exception(InvalidTaskVariable, tok) }
      cAssert(varNumber > 0, InvalidTaskVariable, tok)
      newToken(new prim._taskvariable(varNumber),
               ident, TokenType.REPORTER, tok.startPos, tok.endPos, tok.fileName)
    }
    // kludgy to special case this, but we only have one such prim,
    // so oh well... - ST 7/8/06
    else if(ident == "RANDOM-OR-RANDOM-FLOAT")
      exception(RandomOrRandomFloatError, tok)
    else if(getLetFromArg(procedure, ident, tokPos).isDefined)
      newToken(new prim._letvariable(getLetFromArg(procedure, ident, tokPos).get),
               ident, TokenType.REPORTER, tok.startPos, tok.endPos, tok.fileName)
    else if(procedure.args.contains(ident))
      newToken(new prim._procedurevariable(procedure.args.indexOf(ident), ident),
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
            (TokenType.REPORTER, new prim._callreport(callproc))
          else
            (TokenType.COMMAND, new prim._call(callproc))
        newToken(caller, ident, tokenType, tok.startPos, tok.endPos, tok.fileName)
      }
    }
  }

  private def getAgentVariableReporter(varName: String, tok: Token): Reporter = {
    if(program.turtlesOwn.contains(varName) && program.linksOwn.contains(varName))
      new prim._turtleorlinkvariable(varName)
    else if(program.turtlesOwn.contains(varName))
      new prim._turtlevariable(program.turtlesOwn.indexOf(varName))
    else if(program.patchesOwn.contains(varName))
      new prim._patchvariable(program.patchesOwn.indexOf(varName))
    else if(program.linksOwn.contains(varName))
      new prim._linkvariable(program.linksOwn.indexOf(varName))
    else if(program.globals.contains(varName))
      new prim._observervariable(program.globals.indexOf(varName))
    else if(program.breeds.values.exists(_.owns.contains(varName)))
      new prim._breedvariable(varName)
    else if(program.linkBreeds.values.exists(_.owns.contains(varName)))
      new prim._linkbreedvariable(varName)
    else if(forgiving)
      new prim._unknownidentifier
    else
      exception("Nothing named " + varName + " has been defined",
                Token(varName, tok.tpe, tok.value)(
                  tok.startPos, tok.startPos + varName.length, tok.fileName))
  }

  private def checkProcedureName(procedure: Procedure) {
    val newVal: AnyRef =
      // if the proc name doesn't trigger any identifier rules it's treated as a variable reference,
      // and if there's no variable with that name, CompilerException is raised -- CLB
      try processToken2(procedure.nameToken, procedure, 0).value
      catch { case ex: CompilerException => return }
    val ok = newVal.isInstanceOf[prim._call] ||
      newVal.isInstanceOf[prim._callreport] ||
      newVal.isInstanceOf[prim._unknownidentifier]
    cAssert(ok, "Cannot use " + procedure.name + " as a procedure name.  Conflicts with: " + newVal,
            procedure.nameToken)
  }

  private def newToken(instr: Instruction, name: String, tpe: TokenType,
      startPos: Int, endPos: Int, fileName: String) = {
    val tok = Token(name, tpe, instr)(startPos, endPos, fileName)
    instr.token(tok)
    tok
  }

  /// error texts
  private val InvalidTaskVariable =
    "variables may not begin with a question mark unless they are the special variables ?, ?1, ?2, ..."

  private val RandomOrRandomFloatError =
    "This code was written for an old version of NetLogo in which the RANDOM primitive sometimes reported " +
    "an integer (e.g. 4), other times a floating point number (e.g. 4.326), depending on its input. " +
    "That's no longer true in this version; instead, we now have two separate primitives. So you must " +
    "replace this with either RANDOM or RANDOM-FLOAT depending on whether you want an integer or " +
    "a floating point result."

}
