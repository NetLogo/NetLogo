// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Color
import org.nlogo.api.{ CompilerServices, Token, TokenType }
import org.nlogo.editor.Colorizer
import collection.JavaConverters._

class EditorColorizer(compiler: CompilerServices) extends Colorizer[TokenType] {

  // cache last studied line, so we don't retokenize the same string over and over again when the
  // user isn't even doing anything
  private var lastLine = ""
  private var lastColors = Array[Color]()

  def reset() {
    lastLine = ""
    lastColors = Array()
  }

  def getCharacterColors(line: String): Array[Color] =
    if (line == lastLine)
      lastColors
    else {
      val tokens = tokenizeForColorization(line)
      val result = Array.fill(line.size)(SyntaxColors.DEFAULT_COLOR)
      for (tok <- tokens) {
        // "breed" can be either a keyword or a turtle variable, which means we can't reliably
        // colorize it correctly; so as a kludge we colorize it as a keyword if it's right at the
        // beginning of the line (position 0) - ST 7/11/06
        val color = getTokenColor(
          if (tok.tyype == TokenType.VARIABLE &&
              tok.startPos == 0 &&
              tok.name.equalsIgnoreCase("BREED"))
            TokenType.KEYWORD
          else
            tok.tyype
        )
        for (j <- tok.startPos until tok.endPos)
          // guard against any bugs in tokenization causing out-of-bounds positions
          if(result.isDefinedAt(j))
            result(j) = color
      }
      lastColors = result
      lastLine = line
      result
    }

  // This is used for bracket matching and word selection (double clicking) and not for
  // colorization, so we don't need to bother with the TYPE_KEYWORD hack for "breed" here.
  // - ST 7/11/06
  def getCharacterTokenTypes(line: String): java.util.List[TokenType] = {
    val result = new Array[TokenType](line.size)
    val tokens = tokenizeForColorization(line)
    for {tok <- tokens; j <- tok.startPos until tok.endPos}
      // guard against any bugs in tokenization causing out-of-bounds positions
      if (result.isDefinedAt(j))
        result(j) = tok.tyype
    result.toIndexedSeq.asJava
  }

  def isMatch(token1: TokenType, token2: TokenType) =
    (token1, token2) == (TokenType.OPEN_PAREN, TokenType.CLOSE_PAREN) ||
    (token1, token2) == (TokenType.OPEN_BRACKET, TokenType.CLOSE_BRACKET)

  def isOpener(token: TokenType) =
    token == TokenType.OPEN_PAREN || token == TokenType.OPEN_BRACKET

  def isCloser(token: TokenType) =
    token == TokenType.CLOSE_PAREN || token == TokenType.CLOSE_BRACKET

  def tokenizeForColorization(line: String): Array[Token] =
    compiler.tokenizeForColorization(line)

  ///

  private def getTokenColor(tyype: TokenType) =
    tyype match {
      case TokenType.CONSTANT =>
        SyntaxColors.CONSTANT_COLOR
      case TokenType.COMMAND =>
        SyntaxColors.COMMAND_COLOR
      case TokenType.REPORTER =>
        SyntaxColors.REPORTER_COLOR
      case TokenType.VARIABLE =>
        SyntaxColors.REPORTER_COLOR
      case TokenType.KEYWORD =>
        SyntaxColors.KEYWORD_COLOR
      case TokenType.COMMENT =>
        SyntaxColors.COMMENT_COLOR
      case _ =>
        SyntaxColors.DEFAULT_COLOR
    }

  def getTokenAtPosition(text: String, position: Int): String =
    Option(compiler.getTokenAtPosition(text, position))
      .map(_.name).orNull

  def doHelp(comp: java.awt.Component, name: String) {
    QuickHelp.doHelp(comp, name)
  }

}
