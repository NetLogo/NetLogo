// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import java.io.Reader

trait TokenizerInterface {
  def tokenizeString(source: String, filename: String = ""): Iterator[Token]
  def tokenize(reader: Reader, filename: String = ""): Iterator[Token]
  def getTokenAtPosition(source: String, position: Int): Option[Token]
  def isValidIdentifier(ident: String): Boolean
  def tokenizeSkippingTrailingWhitespace(reader: java.io.Reader, filename: String = ""): Iterator[(Token, Int)]
}
