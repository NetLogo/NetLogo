// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import CompilerExceptionThrowers.{ cAssert, exception }
import org.nlogo.agent.{ Agent, Link, Turtle }
import org.nlogo.api.{ ErrorSource, ExtensionManager, Let,
                       Program, Token, TokenizerInterface, TokenType }
import org.nlogo.nvm.{ Instruction, Procedure }
import org.nlogo.prim._let

private object StructureParser {

  class Results(val procedures: java.util.Map[String, Procedure],
                val tokens: Map[Procedure, Iterable[Token]])

  def resolvePath(fileName: String, path: String): String = {
    val pathFile = new java.io.File(path)
    val rootFile = new java.io.File(fileName)
    if(pathFile.isAbsolute) path
    else {
      val result = new java.io.File(rootFile.getParentFile,path)
      try result.getCanonicalPath
      catch {
        case ex:java.io.IOException =>
          org.nlogo.util.Exceptions.ignore(ex)
          result.getPath
      }
    }
  }
  // It's not the greatest that we have this instead of just using a standard collection
  // class directly, but it's confined to StructureParser (it used to be used by other
  // phases of compilation as well) so it's not that important. - ST 12/22/08
  private class TokenBuffer {
    private val tokens =
      new collection.mutable.ArrayBuffer[Token]
    def slice(i1: Int, i2: Int) =
      tokens.slice(i1, i2).toList ::: List(Token.eof)
    // iteration
    var index = 0
    def hasNext = index < tokens.size
    def next() =
      if(hasNext) { index += 1; tokens(index - 1) }
      else Token.eof
    def head =
      if(hasNext) tokens(index)
      else Token.eof
    // mutation
    def add(t: Token) { tokens += t }
    def appendAll(ts: Iterable[Token]) {
      tokens ++= ts.takeWhile(_.tyype != TokenType.EOF)
    }
  }
}

/**
 * Calling parse() has no side effects, it just constructs a Results object.  Exception: the
 * ExtensionManager gets side effected (it loads and unloads extensions).
 */
private class StructureParser(
  originalTokens: Seq[Token],
  displayName: Option[String],
  program: Program,
  oldProcedures: java.util.Map[String, Procedure],
  extensionManager: ExtensionManager)
(implicit tokenizer: TokenizerInterface) {

  import StructureParser.TokenBuffer

  private val tokenBuffer = new TokenBuffer { appendAll(originalTokens) }
  private val tokensMap = new collection.mutable.HashMap[Procedure, Iterable[Token]]

  // use LinkedHashMap so procedures come out in the order they were defined (users expect errors in
  // earlier procedures to be reported first) - ST 6/10/04
  private val newProcedures = new java.util.LinkedHashMap[String, Procedure]

  def parse(subprogram: Boolean): StructureParser.Results = {
    // Warning, incredibly confusing kludginess ahead...  In the usingFiles variable, it would be
    // simpler just to store the strings of the paths we want to load, but if we can't find one of
    // the paths, we want to report an error that refers to the original __include statement, so we
    // store paths paired with tokens.  But sometimes we don't have a real token, because some code
    // isn't in an include file at all; it's in the main Code tab (or button or whatever) or
    // it comes from the system dynamics modeler.  In those cases the token is null and the path is
    // the empty string.  Also confusing is that
    // we're iterating over usingFiles at the same time that we're adding entries to it; that's
    // because we are discovering the __include calls in the code as we go.  The index variable
    // keeps track of our current location in usingFiles.  All of this is very confusing and ought
    // to be cleaned up. - ST 12/19/08
    val usingFiles = new collection.mutable.ArrayBuffer[(String, Token)]
    var fileName = ""
    var index = 1
    usingFiles += (("", null))
    var totallyDone = false
    while(!totallyDone) {
      var haveGlobals = subprogram
      var haveTurtlesOwn = subprogram
      var havePatchesOwn = subprogram
      var haveLinksOwn = subprogram
      var haveIncludes = subprogram
      var done = false
      while(!done && tokenBuffer.hasNext) {
        val token = tokenBuffer.head
        // kludge: special case because of naming conflict with BREED turtle variable - jrn 8/04/05
        if(token.tyype == TokenType.VARIABLE && token.value == "BREED") {
          tokenBuffer.next()
          val breedList = new java.util.ArrayList[String]
          parseVarList(breedList, null, null)
          try {
            if(java.lang.Boolean.getBoolean("org.nlogo.lang.requireSingularBreedArgument"))
              cAssert(breedList.size == 2,
                      "breed requires a singular form since org.nlogo.lang.requireSingularBreedArgument is true",
                      token)
          }
          catch {
            // can't check arbitrary properties from applets
            case ex:java.security.AccessControlException =>
              org.nlogo.util.Exceptions.ignore(ex)
          }
          finally { cAssert(breedList.size == 1 || breedList.size == 2,
                            "breed only takes 1 or 2 inputs",token) }
          val breedName = breedList.get(0)
          program.breeds.put(breedName, breedName) // will replace with agentset at realloc time
          program.breedsOwn.put(breedName, new java.util.ArrayList[String])
          if(breedList.size == 2)
            program.breedsSingular.put(breedList.get(1), breedName)
        }
        else {
          cAssert(token.tyype == TokenType.KEYWORD,"Expected keyword",token)
          val keyword = token.value.asInstanceOf[String]
          if(keyword == "TO" || keyword == "TO-REPORT")
            parseProcedure(token)
          else if(keyword == "DIRECTED-LINK-BREED" || keyword == "UNDIRECTED-LINK-BREED") {
            tokenBuffer.next()
            val breedList = new java.util.ArrayList[String]
            parseVarList(breedList, null, null)
            cAssert(breedList.size == 2, keyword + " only takes 2 inputs", token)
            val breedName = breedList.get(0)
            // will replace with agentset at realloc time
            program.linkBreeds.put(breedName, keyword)
            program.linkBreedsOwn.put(breedName, new java.util.ArrayList[String])
            if(breedList.size == 2)
              program.linkBreedsSingular.put(breedList.get(1), breedName)
          }
          else if(keyword == "TURTLES-OWN") {
            cAssert(!haveTurtlesOwn,"Redeclaration of TURTLES-OWN",token)
            tokenBuffer.next()
            haveTurtlesOwn = true
            parseVarList(program.turtlesOwn, null, null)
          }
          else if(keyword == "LINKS-OWN") {
            cAssert(!haveLinksOwn,"Redeclaration of LINKS-OWN",token)
            tokenBuffer.next()
            haveLinksOwn = true
            parseVarList(program.linksOwn, null, null)
          }
          else if(keyword == "PATCHES-OWN") {
            cAssert( !havePatchesOwn,"Redeclaration of PATCHES-OWN",token)
            tokenBuffer.next()
            havePatchesOwn = true
            parseVarList( program.patchesOwn, null, null)
          }
          else if(keyword == "GLOBALS") {
            cAssert(!haveGlobals,"Redeclaration of GLOBALS",token)
            tokenBuffer.next()
            haveGlobals = true
            parseVarList(program.globals, null, null)
          }
          else if(keyword.endsWith("-OWN")) {
            val breedName = keyword.substring(0, keyword.length - 4)
            cAssert(program.breeds.containsKey(breedName) || program.linkBreeds.containsKey(breedName),
                    "There is no breed named " + breedName,token)
            tokenBuffer.next()
            var linkbreed = false
            if(program.breedsOwn.containsKey(breedName)) {
              cAssert(program.breedsOwn.get(breedName).isEmpty,
                      "Redeclaration of " + keyword, token)
            }
            else if(program.linkBreedsOwn.containsKey(breedName)) {
              cAssert(program.linkBreedsOwn.get(breedName).isEmpty,
                      "Redeclaration of " + keyword, token)
              linkbreed = true
            }
            val vars = new java.util.ArrayList[String]
            if(linkbreed) {
              parseVarList(vars, classOf[Link], null)
              program.linkBreedsOwn.put(breedName, vars)
            }
            else {
              parseVarList(vars, classOf[Turtle], null)
              program.breedsOwn.put(breedName, vars)
            }
          }
          else if(keyword == "EXTENSIONS")
            parseImport(tokenBuffer)
          else if(keyword == "__INCLUDES") {
            cAssert(!haveIncludes,"Redeclaration of __INCLUDES",token)
            haveIncludes = true
            var filePath: String = null
            tokenBuffer.next()
            // Retrieve the Using File Path
            cAssert(tokenBuffer.head.tyype == TokenType.OPEN_BRACKET,
                    "Expected [",tokenBuffer.head)
            tokenBuffer.next()
            while(tokenBuffer.head.tyype != TokenType.CLOSE_BRACKET) {
              var pathToken = tokenBuffer.head
              cAssert(pathToken.tyype == TokenType.CONSTANT && pathToken.value.isInstanceOf[String],
                      "Expected string or ]", pathToken)
              val name = pathToken.value.asInstanceOf[String]
              cAssert(name.endsWith(".nls"),"Included files must end with .nls", pathToken)
              pathToken = tokenBuffer.next()
              filePath =
                if(fileName.isEmpty)
                  // because extensionManager has a ref to the workspace and thus the modelDir, which
                  // is what we resolve against if we have no filename set -- CLB 02/01/05
                  extensionManager.resolvePath(pathToken.value.asInstanceOf[String])
                else
                  StructureParser.resolvePath(fileName, pathToken.value.asInstanceOf[String])
              if(filePath != null && !usingFiles.exists(_._1 == filePath))
                usingFiles += ((filePath, pathToken))
            }
            tokenBuffer.next() // eat close bracket
          }
          else exception("Expected procedure or variable declaration",token)
        }
      }
      if(index < usingFiles.size) {
        fileName = usingFiles(index)._1
        val source:String =
          try extensionManager.getSource(fileName)
          catch { case _:java.io.IOException =>
            exception("Could not find " + fileName,usingFiles(index)._2) }
        tokenBuffer.appendAll(tokenizer.tokenize(source, fileName))
        index += 1
      }
      else totallyDone = true
    }
    if(!subprogram)
      extensionManager.finishFullCompilation()
    new StructureParser.Results(newProcedures, tokensMap.toMap)
  }
  // replaces an identifier token with its imported implementation, if necessary
  private def processTokenWithExtensionManager(token: Token): Token = {
    def wrap(prim: org.nlogo.api.Primitive, name: String): Instruction =
      prim match {
        case c: org.nlogo.api.Command  => new org.nlogo.prim._extern(c)
        case r: org.nlogo.api.Reporter => new org.nlogo.prim._externreport(r)
      }
    if(token.tyype != TokenType.IDENT ||
       extensionManager == null || !extensionManager.anyExtensionsLoaded)
      token
    else {
      val name = token.value.asInstanceOf[String]
      val replacement = extensionManager.replaceIdentifier(name)
      replacement match {
        // if there's no replacement, make no change.
        case null => token
        case prim =>
          val newType = if(prim.isInstanceOf[org.nlogo.api.Command]) TokenType.COMMAND
                        else TokenType.REPORTER
          val instruction = wrap(prim,name)
          val newToken = new Token(token.name, newType, instruction)(token.startPos, token.endPos, token.fileName)
          instruction.token(newToken)
          newToken
      }
    }
  }
  private def parseProcedure(firstToken: Token): Procedure = {
    var isReporterProcedure = false
    var startPos: Int = 0
    var endPos: Int = 0
    var done = false
    var haveTo = false
    var haveName = false
    var haveArgList = false
    var haveLocals = false
    var haveEnd = false
    var procedure: Procedure = null
    var start = 0
    while(!done) {
      val token = tokenBuffer.head
      if(token.tyype == TokenType.EOF) {
        val msg = "Last procedure doesn't end with END"
        if(haveName)
          // non-recommended call here, but less hassle...
          exception(msg, procedure.pos,procedure.endPos,procedure.fileName)
        else
          exception(msg, firstToken)
      }
      if(!haveTo) {
        cAssert(token.tyype == TokenType.KEYWORD, "Expected TO or TO-REPORT", token)
        val keyword = token.value.asInstanceOf[String]
        if(keyword == "TO" || keyword == "TO-REPORT")
          isReporterProcedure = keyword == "TO-REPORT"
        else exception("Expected TO or TO-REPORT",token)
        tokenBuffer.next()
        haveTo = true
        startPos = token.startPos
        // we set some value for this here, so that if we never get around to overwriting it later
        // because an error occurs, the highlighting is still nice.
        endPos = token.endPos
      }
      else if(!haveName) {
        cAssert(token.tyype == TokenType.IDENT,"You can't use " + token.name.toUpperCase + " to name a procedure", token)
        tokenBuffer.next()
        haveName = true
        procedure = new Procedure(
          if(isReporterProcedure) Procedure.Type.REPORTER
          else Procedure.Type.COMMAND,
          token, token.name.toUpperCase, displayName, null)
        checkName(procedure.name, token, null, null)
        cAssert(newProcedures.get(procedure.name) == null, "Cannot redefine " + procedure.name, token)
        // we set this here, so that if an error occurs, the highlighting is nice.
        procedure.endPos = token.endPos
      }
      else if(!haveArgList) {
        if(token.tyype == TokenType.OPEN_BRACKET) {
          parseVarList(procedure.args, null, procedure)
          start = tokenBuffer.index
        }
        haveArgList = true
      }
      else if(!haveLocals) {
        if(token.tyype == TokenType.KEYWORD) {
          val keyword = token.value.asInstanceOf[String]
          if(keyword == "END") {
            if(start == 0) start = tokenBuffer.index
            tokensMap.put(procedure, tokenBuffer.slice(start, tokenBuffer.index).map(processTokenWithExtensionManager))
            tokenBuffer.next()
            procedure.endPos = token.startPos
            done = true
          }
          else exception("This doesn't make sense here", token)
        }
        else {
          start = tokenBuffer.index
          haveLocals = true
        }
      }
      else if(!haveEnd) {
        if(token.tyype == TokenType.COMMAND && token.value.isInstanceOf[_let])
          parseLet(procedure, start, new java.util.ArrayList[String])
        else if(token.tyype == TokenType.KEYWORD) {
          val keyword = token.value.asInstanceOf[String]
          if(keyword == "END") {
            if(start == 0)
              start = tokenBuffer.index
            procedure.endPos = token.startPos
            tokensMap.put(procedure,tokenBuffer.slice(start, tokenBuffer.index).map(processTokenWithExtensionManager))
            done = true
            tokenBuffer.next()
          }
          else exception("This doesn't make sense here", token)
        }
        else tokenBuffer.next()
      }
    }
    newProcedures.put(procedure.name, procedure)
    procedure
  }
  private def parseVarList(result: java.util.List[String], owningAgentClass: Class[_ <: Agent], procedure: Procedure) {
    var token = tokenBuffer.next()
    cAssert(token.tyype == TokenType.OPEN_BRACKET, "Expected [", token)
    var done = false
    while(!done) {
      token = tokenBuffer.next()
      if(token.tyype == TokenType.CLOSE_BRACKET)
        done = true
      else {
        cAssert(token.tyype != TokenType.COMMAND,
                "There is already a primitive with that name",token)
        cAssert(token.tyype != TokenType.REPORTER,
                "There is already a primitive with that name",token)
        cAssert(token.tyype != TokenType.KEYWORD,
                "There is already a keyword with that name",token)
        cAssert(token.tyype == TokenType.IDENT,
                "Expected name or ]",token)
        cAssert(newProcedures.get(token.name.toUpperCase) == null,
                "There is already a procedure with that name",token)
        cAssert(!result.contains(token.value),
                "The name " + token.value + " is already defined",token)
        checkName(token.value.asInstanceOf[String], token, owningAgentClass, procedure)
        result.add(token.value.asInstanceOf[String])
      }
    }
  }
  private def checkName(varName: String, token: Token, owningAgentClass: Class[_ <: Agent], procedure: Procedure) {
    if(owningAgentClass == null || owningAgentClass == classOf[Link]) {
      val keys = program.breedsOwn.keySet.iterator()
      while(keys.hasNext) {
        val breedName = keys.next()
        val breedOwns = program.breedsOwn.get(breedName)
        cAssert(!breedOwns.contains(varName),
                "You already defined " + varName + " as a " + breedName + " variable", token)
      }
    }
    if(owningAgentClass == null || owningAgentClass == classOf[Turtle]) {
      val keys = program.linkBreedsOwn.keySet.iterator()
      while(keys.hasNext) {
        val breedName = keys.next()
        val breedOwns = program.linkBreedsOwn.get(breedName)
        cAssert(!breedOwns.contains(varName),
                "You already defined " + varName + " as a " + breedName + " variable", token)
      }
    }
    cAssert(!program.turtlesOwn.contains(varName),
            "There is already a turtle variable called " + varName, token)
    cAssert(!program.patchesOwn.contains(varName),
            "There is already a patch variable called " + varName, token)
    cAssert(!program.globals.contains(varName),
            "There is already a global variable called " + varName, token)
    cAssert(!program.breeds.containsKey(varName),
            "There is already a breed called " + varName, token)
    cAssert(!program.linkBreeds.containsKey(varName),
            "There is already a link breed called " + varName, token)
    if(procedure != null)
      {
        cAssert(varName != procedure.name,
                "There is already a procedure with that name", token)
        cAssert(!procedure.args.contains(varName),
                "There is already a local variable called " + varName + " here", token)
      }
    checkNameAgainstProceduresMap(varName, token, oldProcedures, procedure != null)
    checkNameAgainstProceduresMap(varName, token, newProcedures, procedure != null)
  }

  private def checkNameAgainstProceduresMap(varName: String, token: Token, procedures: java.util.Map[String, Procedure], isLocal: Boolean) {
    cAssert(!procedures.containsKey(varName),"There is already a procedure with that name", token)
    if(!isLocal) {
      val iter = procedures.keySet.iterator()
      while(iter.hasNext) {
        val proc = procedures.get(iter.next())
        cAssert(!proc.args.contains(varName),
                "There is already a local variable called " + varName + " in the " + proc.name + " procedure",token)
        val iter2 = proc.lets.iterator()
        while(iter2.hasNext)
          cAssert(varName != iter2.next().varName,
                  "There is already a local variable called " + varName + " in the " +
                  proc.name + " procedure",token)
      }
    }
  }
  /**
   * parses the "import" special form
   */
  private def parseImport(tokenBuffer: TokenBuffer) {
    extensionManager.startFullCompilation()
    tokenBuffer.next() // skip the __extensions
    val token = tokenBuffer.next()
    cAssert(token.tyype == TokenType.OPEN_BRACKET, "Expected [", token)
    var done = false
    while(!done) {
      val token = tokenBuffer.next()
      if(token.tyype == TokenType.CLOSE_BRACKET)
        done = true
      else {
        cAssert(token.tyype == TokenType.IDENT && token.name != null, "Expected identifier or ]", token)
        extensionManager.importExtension(
          token.value.asInstanceOf[String].toLowerCase,
          new ErrorSource(token))
      }
    }
  }
  private def parseLet(procedure: Procedure, offset: Int, oldAncestorNames: java.util.List[String]): Let = {
    var ancestorNames = oldAncestorNames
    val letToken = tokenBuffer.next()
    val nameToken = tokenBuffer.head
    cAssert(nameToken.tyype == TokenType.IDENT,"Expected variable name here", nameToken)
    val name = nameToken.value.asInstanceOf[String]
    val startPos = tokenBuffer.index - offset
    cAssert(!ancestorNames.contains(name),
            "There is already a local variable called " + name + " here", nameToken)
    checkName(name, nameToken, null, procedure)
    var level = 1
    val children = new collection.mutable.ListBuffer[Let]
    def newLet(endPos: Int) = {
      import collection.JavaConverters._
      val result = new Let(name, startPos, endPos, children.asJava)
      letToken.value.asInstanceOf[_let].let = result
      procedure.lets.add(result)
      result
    }
    while(true) {
      if(!tokenBuffer.hasNext)
        exception("Expected ] or END", tokenBuffer.head)
      val token = tokenBuffer.head
      if(token.tyype == TokenType.OPEN_BRACKET) {
        level += 1
        tokenBuffer.next()
      }
      else if(token.tyype == TokenType.CLOSE_BRACKET) {
        level -= 1
        if(level == 0) return newLet(tokenBuffer.index - offset)
        tokenBuffer.next()
      }
      else if(token.tyype == TokenType.COMMAND && token.value.isInstanceOf[_let]) {
        ancestorNames = new java.util.ArrayList[String](ancestorNames)
        ancestorNames.add(name)
        children += parseLet(procedure, offset, ancestorNames)
      }
      else if(token.tyype == TokenType.KEYWORD) {
        val keyword = token.value.asInstanceOf[String]
        if(keyword != "END") exception("Expected ] or END", token)
        return newLet(tokenBuffer.index - offset)
      }
      else tokenBuffer.next()
    }
    throw new IllegalStateException
  }
}

private class StructureParserExtras(implicit tokenizer: TokenizerInterface) {
  /**
   * identifies the positions of all procedure definitions in the given
   * source. Returns a Map mapping String procedure names to Lists.
   * Each List contains 4 elements: the String procedure name, the Integer
   * position of the "to" or "to-report" keyword, the Integer position of the
   * procedure name, and the Integer position of the "end" keyword.
   *
   * This data structure is used to populate the "procedures" menu in the
   * GUI.
   */
  def findProcedurePositions(source: String): java.util.Map[String, java.util.List[AnyRef]] = {
    val procsTable = new java.util.HashMap[String,java.util.List[AnyRef]]
    // Tokenize the current procedures window source
    val tokens = tokenizer.tokenizeRobustly(source).iterator.buffered
    while(tokens.hasNext) {
      var token = tokens.next()
      if(token.tyype == TokenType.KEYWORD) {
        val keyword = token.value.asInstanceOf[String]
        if(keyword == "TO" || keyword == "TO-REPORT") {
          // position of to/to-report
          val toPos = token.startPos
          // name of procedure
          val nameToken = tokens.head
          if(nameToken.tyype == TokenType.IDENT) {
            val name = nameToken.name
            val namePos = nameToken.startPos
            // position of end
            var done = false
            while(!done && tokens.hasNext) {
              token = tokens.next()
              if(token.tyype == TokenType.KEYWORD && token.value == "END")
                done = true
            }
            val endPos = token.endPos
            // build index
            val index = new java.util.ArrayList[AnyRef](4)
            index.add(name)
            index.add(Int.box(toPos))
            index.add(Int.box(namePos))
            index.add(Int.box(endPos))
            procsTable.put(name, index)
          }
        }
      }
    }
    procsTable
  }
  def findIncludes(sourceFileName: String, source: String): java.util.Map[String, String] = {
    val includedFiles = new java.util.HashMap[String, String]
    // Tokenize the current procedures window source
    val myTokens = tokenizer.tokenizeRobustly(source).iterator.buffered
    while(myTokens.hasNext) {
      val token = myTokens.next()
      if(token.tyype == TokenType.KEYWORD) {
        val keyword = token.value.asInstanceOf[String]
        if(keyword == "__INCLUDES") {
          while(true) {
            var filePath: String = null
            var pathToken = myTokens.head
            if(pathToken.tyype == TokenType.OPEN_BRACKET) {
              myTokens.next()
              pathToken = myTokens.head
            }
            else if(pathToken.tyype == TokenType.CLOSE_BRACKET)
              return includedFiles
            else if(pathToken.tyype == TokenType.CONSTANT && pathToken.value.isInstanceOf[String]) {
              pathToken = myTokens.next()
              filePath = StructureParser.resolvePath(sourceFileName, pathToken.value.asInstanceOf[String])
              includedFiles.put(pathToken.value.asInstanceOf[String], filePath)
            }
            else
              return includedFiles
          }
        }
      }
    }
    includedFiles
  }
}
