// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Color

import org.nlogo.core.{ Token, TokenType }
import org.nlogo.api.{ EditorCompiler, ExtensionManager }
import org.nlogo.editor.Colorizer
import org.nlogo.workspace.AbstractWorkspace

import collection.JavaConverters._

object DefaultEditorColorizer {
  def apply(workspace: AbstractWorkspace): EditorColorizer = new EditorColorizer(workspace.compiler, workspace.getExtensionManager)
}

class EditorColorizer(compiler: EditorCompiler, extensionManager: ExtensionManager) extends Colorizer {
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
          if (tok.tpe == TokenType.Reporter &&
              tok.start == 0 &&
              tok.text.equalsIgnoreCase("BREED"))
            TokenType.Keyword
          else
            tok.tpe
        ).getOrElse(SyntaxColors.DEFAULT_COLOR)
        for (j <- tok.start until tok.end)
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
    for {tok <- tokens; j <- tok.start until tok.end}
      // guard against any bugs in tokenization causing out-of-bounds positions
      if (result.isDefinedAt(j))
        result(j) = tok.tpe
    result.toIndexedSeq.asJava
  }

  def isMatch(token1: TokenType, token2: TokenType) =
    (token1, token2) == ((TokenType.OpenParen, TokenType.CloseParen)) ||
    (token1, token2) == ((TokenType.OpenBracket, TokenType.CloseBracket))

  def isOpener(token: TokenType) =
    token == TokenType.OpenParen || token == TokenType.OpenBracket

  def isCloser(token: TokenType) =
    token == TokenType.CloseParen || token == TokenType.CloseBracket

  def tokenizeForColorization(line: String): Array[Token] =
    compiler.tokenizeForColorization(line, extensionManager)

  ///

  private val tokenTypeColors = Map[TokenType, java.awt.Color](
    TokenType.Literal  -> SyntaxColors.CONSTANT_COLOR,
    TokenType.Command  -> SyntaxColors.COMMAND_COLOR,
    TokenType.Reporter -> SyntaxColors.REPORTER_COLOR,
    TokenType.Keyword  -> SyntaxColors.KEYWORD_COLOR,
    TokenType.Comment  -> SyntaxColors.COMMENT_COLOR
  )

  private def getTokenColor(tpe: TokenType): Option[java.awt.Color] =
    tokenTypeColors.get(tpe)

  def getTokenAtPosition(text: String, position: Int): Option[String] =
    Option(compiler.getTokenAtPosition(text, position))
      .map(_.text)

  def doHelp(comp: java.awt.Component, name: String) {
    QuickHelp.doHelp(comp, name, compiler.defaultDialect.is3D)
  }

}
