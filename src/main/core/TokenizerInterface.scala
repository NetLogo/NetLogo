// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

trait TokenizerInterface {
  def tokenizeString(source: String, filename: String = ""): Iterator[Token]
  def tokenize(reader: java.io.Reader, filename: String = ""): Iterator[Token]
}
