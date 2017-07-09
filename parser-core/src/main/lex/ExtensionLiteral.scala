// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.nlogo.core.{ SourceLocation, Token, TokenType }

class ExtensionLiteral(start: Int, filename: String) {

  private val builder = new StringBuilder
  private var nestingLevel = 0

  def isFinished = nestingLevel == 0

  def add(s: String) { builder.append(s) }
  def push() { nestingLevel += 1 }
  def pop() { nestingLevel -= 1 }

  def token(): Token = {
    val text = builder.toString
    Token(text, TokenType.Extension, text)(SourceLocation(start, start + text.size, filename))
  }

}
