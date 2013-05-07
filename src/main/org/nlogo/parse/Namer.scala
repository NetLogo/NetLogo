// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.{ api, nvm, parse0, prim },
  api.{ Token, TokenType },
  nvm.ParserInterface.ProceduresMap,
  Fail._

// This class is in serious need of a total rewrite - ST 5/3/13

/**
  * Converts identifier tokens into instances of primitives.
  */

class Namer(
  program: api.Program,
  procedures: ProceduresMap,
  extensionManager: api.ExtensionManager,
  lets: Vector[api.Let]) {

  def process(tokens: Iterator[Token], procedure: nvm.Procedure): Iterator[Token] = {
    // make sure the procedure name doesn't conflict with a special identifier -- CLB
    checkProcedureName(procedure)
    val it = new parse0.CountedIterator(tokens)
    def processToken(token: Token): Token =
      token.tpe match {
        case TokenType.Ident =>
          processIdent(token, procedure, it.count)
        case _ =>
          token
      }
    def stuffLet(token: Token): Token = {
      (token.tpe, token.value) match {
        case (TokenType.Command, let: prim._let) =>
          // LetScoper constructed Let objects, but it didn't stash them
          // in the prim._let objects. we do that here, so that LetScoper
          // doesn't depend on prim._let - ST 5/2/13
          let.let = lets.find(let => let.start == it.count + 1).get
        case _ =>
      }
      token
    }
    it.map(processTokenWithExtensionManager)
      .map(processToken)
      .map(stuffLet)
  }

  private def processIdent(token: Token, procedure: nvm.Procedure, count: Int): Token = {
    val primName = token.value.asInstanceOf[String]
    def lookup(fn: String => Option[api.TokenHolder], newType: TokenType): Option[Token] =
      fn(primName).map{holder =>
        val newToken =
          token.copy(tpe = newType, value = holder)
        holder.token(newToken)
        newToken
      }
    def command  = lookup(Parser.tokenMapper.getCommand  _, TokenType.Command )
    def reporter = lookup(Parser.tokenMapper.getReporter _, TokenType.Reporter)
    (command orElse reporter).getOrElse(
      processIdent2(token, procedure, count))
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
    if(token.tpe != TokenType.Ident ||
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
              TokenType.Command
            else TokenType.Reporter
          val instruction = wrap(primitive, name)
          val newToken = Token(token.text, newType, instruction)(
            token.start, token.end, token.filename)
          instruction.token(newToken)
          newToken
      }
    }
  }

  private def getLetFromArg(ident: String, tokPos: Int): Option[api.Let] = {
    def checkLet(let: api.Let): Option[api.Let] =
      if(tokPos < let.start || tokPos > let.end || let.name != ident)
        None
      else
        Some(let)
    lets.map(checkLet).find(_.isDefined).getOrElse(None)
  }

  private def processIdent2(tok: Token, procedure: nvm.Procedure, tokPos: Int): Token = {
    assert(tok.tpe == TokenType.Ident)
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
               ident, TokenType.Reporter, tok.start, tok.end, tok.filename)
    }
    // kludgy to special case this, but we only have one such prim,
    // so oh well... - ST 7/8/06
    else if(getLetFromArg(ident, tokPos).isDefined)
      newToken(new prim._letvariable(getLetFromArg(ident, tokPos).get),
               ident, TokenType.Reporter, tok.start, tok.end, tok.filename)
    else if(procedure.args.contains(ident))
      newToken(new prim._procedurevariable(procedure.args.indexOf(ident), ident),
               ident, TokenType.Reporter, tok.start, tok.end, tok.filename)
    else
      // go thru our identifierHandlers, if one triggers, return the result
      parse0.BreedIdentifierHandler.process(tok, program) match {
        case Some((className, breedName, tokenType)) =>
          val instr = Instantiator.newInstance[api.TokenHolder](
            Class.forName("org.nlogo.prim." + className), breedName)
          val tok2 = new Token(tok.text, tokenType, instr)(
            tok.start, tok.end, tok.filename)
          instr.token(tok2)
          tok2
        case None =>
          val callproc =
            procedures.getOrElse(ident,
              return newToken(getAgentVariableReporter(ident, tok),
                              ident, TokenType.Reporter, tok.start, tok.end, tok.filename))
          val (tokenType, caller) =
            if (callproc.isReporter)
              (TokenType.Reporter, new prim._callreport(callproc))
            else
              (TokenType.Command, new prim._call(callproc))
          newToken(caller, ident, tokenType, tok.start, tok.end, tok.filename)
      }
  }

  private def getAgentVariableReporter(varName: String, tok: Token): nvm.Reporter = {
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
    else
      exception("Nothing named " + varName + " has been defined",
                Token(varName, tok.tpe, tok.value)(
                  tok.start, tok.start + varName.length, tok.filename))
  }

  private def checkProcedureName(procedure: nvm.Procedure) {
    val newVal: AnyRef =
      // if the proc name doesn't trigger any identifier rules it's treated as a variable reference,
      // and if there's no variable with that name, CompilerException is raised -- CLB
      try processIdent(procedure.nameToken, procedure, 0).value
      catch { case ex: api.CompilerException => return }
    val ok = newVal.isInstanceOf[prim._call] ||
      newVal.isInstanceOf[prim._callreport]
    cAssert(ok, "Cannot use " + procedure.name + " as a procedure name.  Conflicts with: " + newVal,
            procedure.nameToken)
  }

  private def newToken(instr: nvm.Instruction, name: String, tpe: TokenType,
      start: Int, end: Int, filename: String) = {
    val tok = Token(name, tpe, instr)(start, end, filename)
    instr.token(tok)
    tok
  }

  /// error texts
  private val InvalidTaskVariable =
    "variables may not begin with a question mark unless they are the special variables ?, ?1, ?2, ..."

}
