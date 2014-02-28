// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.nlogo.api, api.{ Token, TokenType }

class ExtensionLiteral(start: Int, filename: String) {

  private val builder = new StringBuilder
  var nestingLevel = 0

  def add(s: String) { builder.append(s) }
  def push() { nestingLevel += 1 }
  def pop() { nestingLevel -= 1 }

  def done(): Token = {
    val text = builder.toString
    Token(text, TokenType.Extension, text)(
      start, start + text.size, filename)
  }

}
