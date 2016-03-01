// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import java.lang.Character

object Charset {
  val identifierPunctuation = """_.?=*!<>:#+/%$^'&-"""
  val digits = "0123456789"

  def validIdentifierChar(c: Char): Boolean =
    digits.contains(c) || identifierPunctuation.contains(c) || Character.isLetter(c)
}
