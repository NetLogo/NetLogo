// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.{ Color, Component }
import java.util.{ List => JList }

import org.nlogo.core.TokenType

trait Colorizer {
  def getCharacterColors(line: String): Array[Color]

  def getCharacterTokenTypes(line: String): JList[TokenType]

  def isMatch(token1: TokenType, token2: TokenType): Boolean

  def isOpener(token: TokenType): Boolean

  def isCloser(token: TokenType): Boolean

  def reset(): Unit // forget any cached information

  // uck. this doesn't really have to do with colorizing
  // but it deals with the tokenizer and we need it in the
  // same spots.  ev 1/17/07
  def getTokenAtPosition(text: String, position: Int): Option[String]

  // this isn't colorizing either.. - ST 3/28/08
  def doHelp(comp: Component, name: String): Unit
}
