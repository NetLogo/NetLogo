// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait TokenizerInterface {
  def tokenizeRobustly(source: String): Seq[Token]
  def tokenize(source: String): Seq[Token]
  def tokenize(source: String, fileName: String): Seq[Token]
  def getTokenAtPosition(source: String, position: Int): Token
  def isValidIdentifier(ident: String): Boolean
  def tokenizeForColorization(source: String): Array[Token]
  def tokenizeForColorization(source: String, extensionManager: ExtensionManager): Array[Token]
  def nextToken(reader: java.io.BufferedReader): Token
  def checkInstructionMaps(): Unit  // for testing
}
