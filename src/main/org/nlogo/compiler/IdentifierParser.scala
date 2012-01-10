// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.compiler.CompilerExceptionThrowers.{cAssert,exception}
import org.nlogo.api.{CompilerException,Let,Program,Token,TokenType}
import org.nlogo.nvm.{Instruction,Procedure,Reporter}
import org.nlogo.prim._
import collection.JavaConverters._

/**
 * Converts identifier tokens into instances of primitives.  In "forgiving" mode, used by
 * AutoConverter, unknown identifiers are assumed to be references to global variables that the
 * compiler doesn't know about yet. - ST 7/7/06 */
private class IdentifierParser(program:Program,
                               oldProcedures:java.util.Map[String,Procedure],
                               newProcedures:java.util.Map[String,Procedure],
                               forgiving:Boolean) {
  def process(tokens: Iterator[Token], procedure: Procedure): Seq[Token] = {
    // make sure the procedure name doesn't conflict with a special identifier -- CLB
    checkProcedureName(procedure)
    val it = new CountedIterator(tokens)
    def processToken(token:Token):Token = {
      // This is pretty gruesome.  In NetLogo 3.1 and earlier, we allowed + to be used with strings
      // and lists.  In order to be able to autoconvert old models, we disable type checking on +
      // when allowRemovedPrimitives is true. - ST 7/8/06, 2/18/08
      if(forgiving && token.value.isInstanceOf[_plus])
        newToken(new dead._pluswildcard,
                 token.name,TokenType.REPORTER,token.startPos,token.endPos,token.fileName)
      else if(token.tyype == TokenType.IDENT || token.tyype == TokenType.VARIABLE)
        processToken2(token,procedure,it.count)
      else token
    }
    it.map(processToken).toSeq
  }
  private def getLetFromArg(p:Procedure,ident:String,tokPos:Int):Option[Let] = {
    def checkLet(let: Let): Option[Let] =
      if(tokPos < let.startPos || tokPos > let.endPos) None
      else let.children.asScala.map(checkLet).find(_.isDefined)
             .getOrElse(if(let.varName == ident) Some(let) else None)
    p.lets.asScala.map(checkLet).find(_.isDefined).getOrElse(None)
  }
  private def processToken2(tok:Token,procedure:Procedure,tokPos:Int):Token = {
    val ident = tok.value.asInstanceOf[String]
    if(ident.startsWith("?")) {
      val varNumber =
        // default number is 1 (i.e., if they just use "?")
        if(ident.length == 1) 1
        // if it's more than just "?", it needs to be an integer.
        else try { Integer.parseInt(ident.substring(1)) }
             catch { case e:NumberFormatException => exception(INVALID_TASK_VARIABLE,tok ) }
      newToken(new _taskvariable(varNumber),
               ident,TokenType.REPORTER,tok.startPos,tok.endPos,tok.fileName)
    }
    // kludgy to special case this, but we only have one such prim,
    // so oh well... - ST 7/8/06
    else if(ident == "RANDOM-OR-RANDOM-FLOAT")
      exception(RANDOM_OR_RANDOM_FLOAT_ERROR,tok)
    else if(getLetFromArg(procedure,ident,tokPos).isDefined)
      newToken(new _letvariable(getLetFromArg(procedure,ident,tokPos).get,ident),
               ident,TokenType.REPORTER,tok.startPos,tok.endPos,tok.fileName)
    else if(procedure.args.contains(ident))
      newToken(new _procedurevariable(procedure.args.indexOf(ident),ident),
               ident,TokenType.REPORTER,tok.startPos,tok.endPos,tok.fileName)
    else {
      // go thru our identifierHandlers, if one triggers, return the result
      BreedIdentifierHandler.process(tok,program).getOrElse{
        val callproc =
          if(oldProcedures.get(ident) != null)
            oldProcedures.get(ident)
          else if(newProcedures.get(ident) != null)
            newProcedures.get(ident)
          else
            return newToken(getAgentVariableReporter(ident,tok),
                            ident,TokenType.REPORTER,tok.startPos,tok.endPos,tok.fileName)
        callproc.tyype match {
          case Procedure.Type.COMMAND =>
            newToken(new _call(callproc),
                     ident,TokenType.COMMAND,tok.startPos,tok.endPos,tok.fileName)
          case Procedure.Type.REPORTER =>
            newToken(new _callreport(callproc),
                     ident,TokenType.REPORTER,tok.startPos,tok.endPos,tok.fileName)
        }
      }
    }
  }

  private def getAgentVariableReporter(varName:String,tok:Token):Reporter = {
    if(program.turtlesOwn.asScala.contains(varName) && program.linksOwn.asScala.contains(varName))
      new _turtleorlinkvariable(varName)
    else if(program.turtlesOwn.asScala.contains(varName))
      new _turtlevariable(program.turtlesOwn.indexOf(varName))
    else if(program.patchesOwn.asScala.contains(varName))
      new _patchvariable(program.patchesOwn.indexOf(varName))
    else if(program.linksOwn.asScala.contains(varName))
      new _linkvariable(program.linksOwn.indexOf(varName))
    else if(program.globals.asScala.contains(varName))
      new _observervariable(program.globals.indexOf(varName))
    else if(program.breedsOwn.asScala.values.exists(_.asScala.contains(varName)))
      new _breedvariable(varName)
    else if(program.linkBreedsOwn.asScala.values.exists(_.asScala.contains(varName)))
      new _linkbreedvariable(varName)
    else if(forgiving)
      new _unknownidentifier
    else
      exception("Nothing named " + varName + " has been defined",
                new Token(varName,tok.tyype,tok.value)
                         (tok.startPos,tok.startPos + varName.length,tok.fileName))
  }
  private def checkProcedureName(procedure:Procedure) {
    val newVal:AnyRef =
      // if the proc name doesn't trigger any identifier rules it's treated as a variable reference,
      // and if there's no variable with that name, CompilerException is raised -- CLB
      try { processToken2(procedure.nameToken,procedure,0).value }
      catch { case ex:CompilerException => return }
    cAssert( ( newVal.isInstanceOf[_call]
              || newVal.isInstanceOf[_callreport]
              || newVal.isInstanceOf[_unknownidentifier]),
            "Cannot use " + procedure.name + " as a procedure name.  Conflicts with: " + newVal,
            procedure.nameToken)
  }
  private def newToken(instr:Instruction,name:String,tyype:TokenType,startPos:Int,endPos:Int,fileName:String) = {
    val tok = new Token(name,tyype,instr)(startPos,endPos,fileName)
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
