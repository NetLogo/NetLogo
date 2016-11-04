// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import java.io.Reader
import java.text.CharacterIterator

trait TokenizerInterface {
  def tokenizeString(source: String, filename: String = ""): Iterator[Token]
  def tokenize(reader: Reader, filename: String = ""): Iterator[Token]
  def getTokenAtPosition(source: String, position: Int): Option[Token]
  def isValidIdentifier(ident: String): Boolean
  def tokenizeSkippingTrailingWhitespace(reader: java.io.Reader, filename: String = ""): Iterator[(Token, Int)]
  // Returns an Iterator[Token] which includes tokens with tpe == TokenType.Whitespace.
  // The other tokenize methods will not include tokens of this type.
  def tokenizeWithWhitespace(reader: Reader, filename: String): Iterator[Token]
  def tokenizeWithWhitespace(source: String, filename: String): Iterator[Token]
  def tokenizeWithWhitespace(iter: CharacterIterator, filename: String): Iterator[Token]
}
