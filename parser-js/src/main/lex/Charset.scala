// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import scala.collection.immutable.HashSet

object Charset {
  val identifierPunctuation = """_.?=*!<>:#+/%$^'&-"""
  val digits = "0123456789"
  val identifierChars = {
    UnicodeInformation.letterCharacters.foldLeft(HashSet[Int]())(_ ++ _.toSet) ++
      (digits.map(_.toInt).toSet ++ identifierPunctuation.map(_.toInt).toSet)
  }

  def validIdentifierChar(c: Char): Boolean = identifierChars.contains(c)
}
