// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse0

import org.nlogo.api.{ TokenType, TokenizerInterface }

// I suppose these could be redone to use parser combinators, like the main StructureParser.  But
// we'd have to use a different grammar because we want these to work even on malformed code that
// StructureParser would refuse to parse. - ST 12/7/12

class StructureLite(tokenizer: TokenizerInterface) {

  def findIncludes(sourceFileName: String, source: String): Map[String, String] = {
    var result = Map[String, String]()
    // Tokenize the current procedures window source
    val myTokens = tokenizer.tokenizeRobustly(source).iterator.buffered
    while(myTokens.hasNext) {
      val token = myTokens.next()
      if(token.tpe == TokenType.Keyword) {
        val keyword = token.value.asInstanceOf[String]
        if(keyword == "__INCLUDES") {
          while(true) {
            var filePath: String = null
            var pathToken = myTokens.head
            if(pathToken.tpe == TokenType.OpenBracket) {
              myTokens.next()
              pathToken = myTokens.head
            }
            else if(pathToken.tpe == TokenType.CloseBracket)
              return result
            else if(pathToken.tpe == TokenType.Literal && pathToken.value.isInstanceOf[String]) {
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
