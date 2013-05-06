// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait TokenizerInterface {
  def tokenize(source: String, fileName: String = ""): Iterator[Token]
  def tokenizeRobustly(reader: java.io.Reader, fileName: String = ""): Iterator[Token]
}
