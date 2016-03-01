// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import FrontEndInterface.ProceduresMap

trait LiteralParser {
  def readFromString(s: String): AnyRef
  def readNumberFromString(source: String): AnyRef
}

trait CompilerUtilitiesInterface extends LiteralParser {
  def readFromString(source: String): AnyRef

  def readNumberFromString(source: String): java.lang.Double

  def readFromString(source: String, importHandler: LiteralImportHandler): AnyRef

  def readNumberFromString(source: String, importHandler: LiteralImportHandler): java.lang.Double

  @throws(classOf[java.io.IOException])
  def readFromFile(currFile: File, importHandler: LiteralImportHandler): AnyRef

  def isReporter(s: String,
                 program: Program,
                 procedures: ProceduresMap,
                 extensionManager: ExtensionManager): Boolean

  def isReporter(s: String): Boolean

  def colorizer: TokenColorizer
}

trait TokenColorizer {
  def toHtml(line: String): String
}
