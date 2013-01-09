package org.nlogo.compiler

import org.nlogo.api.{ TokenType, TokenizerInterface }

// I suppose these could be redone to use parser combinators, like the main StructureParser.  But
// we'd have to use a different grammar because we want these to work even on malformed code that
// StructureParser would refuse to parse. - ST 12/7/12

private class StructureParserExtras(tokenizer: TokenizerInterface) {

  /**
   * identifies the positions of all procedure definitions in the given
   * source. Returns a Map mapping String procedure names to tuples.
   * Each tuple contains 4 elements: the String procedure name, the Int
   * position of the "to" or "to-report" keyword, the Int position of the
   * procedure name, and the Int position of the "end" keyword.
   *
   * This data structure is used to populate the "procedures" menu in the GUI.
   */
  def findProcedurePositions(source: String): Map[String, (String, Int, Int, Int)] = {
    var result = Map[String, (String, Int, Int, Int)]()
    // Tokenize the current procedures window source
    val tokens = tokenizer.tokenizeRobustly(source).iterator.buffered
    while(tokens.hasNext) {
      var token = tokens.next()
      if(token.tpe == TokenType.KEYWORD) {
        val keyword = token.value.asInstanceOf[String]
        if(keyword == "TO" || keyword == "TO-REPORT") {
          // position of to/to-report
          val toPos = token.startPos
          // name of procedure
          val nameToken = tokens.head
          if(nameToken.tpe == TokenType.IDENT) {
            val name = nameToken.name
            // position of end
            var done = false
            while(!done && tokens.hasNext) {
              token = tokens.next()
              if(token.tpe == TokenType.KEYWORD && token.value == "END")
                done = true
            }
            result += name -> (name, toPos, nameToken.startPos, token.endPos)
          }
        }
      }
    }
    result
  }

  def findIncludes(sourceFileName: String, source: String): Map[String, String] = {
    var result = Map[String, String]()
    // Tokenize the current procedures window source
    val myTokens = tokenizer.tokenizeRobustly(source).iterator.buffered
    while(myTokens.hasNext) {
      val token = myTokens.next()
      if(token.tpe == TokenType.KEYWORD) {
        val keyword = token.value.asInstanceOf[String]
        if(keyword == "__INCLUDES") {
          while(true) {
            var filePath: String = null
            var pathToken = myTokens.head
            if(pathToken.tpe == TokenType.OPEN_BRACKET) {
              myTokens.next()
              pathToken = myTokens.head
            }
            else if(pathToken.tpe == TokenType.CLOSE_BRACKET)
              return result
            else if(pathToken.tpe == TokenType.CONSTANT && pathToken.value.isInstanceOf[String]) {
              pathToken = myTokens.next()
              filePath = resolvePath(sourceFileName, pathToken.value.asInstanceOf[String])
              result += pathToken.value.asInstanceOf[String] -> filePath
            }
            else
              return result
          }
        }
      }
    }
    result
  }

  def resolvePath(fileName: String, path: String): String = {
    val pathFile = new java.io.File(path)
    val rootFile = new java.io.File(fileName)
    if (pathFile.isAbsolute)
      path
    else {
      val result = new java.io.File(rootFile.getParentFile, path)
      try result.getCanonicalPath
      catch {
        case ex: java.io.IOException =>
          org.nlogo.util.Exceptions.ignore(ex)
          result.getPath
      }
    }
  }

}
