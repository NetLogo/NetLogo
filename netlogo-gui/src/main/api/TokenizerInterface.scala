// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.Token

trait TokenizerInterface {
  def tokenizeRobustly(source: String): Seq[Token]
  def tokenize(source: String): Seq[Token]
  def tokenize(source: String, fileName: String): Seq[Token]
  def getTokenAtPosition(source: String, position: Int): Token
  def tokenizeForColorization(source: String, extensionManager: ExtensionManager): Array[Token]
}
