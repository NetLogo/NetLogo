// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait TokenizerInterface {
  def tokenize(source: String, fileName: String = ""): Iterator[Token]
  def nextToken(reader: java.io.BufferedReader): Token
}
