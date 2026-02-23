// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import java.awt.Color

import FrontEndInterface.ProceduresMap

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

abstract class ColorizerTheme {
  def getColor(tpe: TokenType): Color
}

object ColorizerTheme {
  val TestTheme = new ColorizerTheme {
    override def getColor(tpe: TokenType): Color = {
      tpe match {
        case TokenType.Literal => new Color(150, 55, 0)
        case TokenType.Command => new Color(0, 0, 170)
        case TokenType.Reporter => new Color(102, 0, 150)
        case TokenType.Keyword => new Color(0, 127, 105)
        case TokenType.Comment => new Color(120, 120, 120)
        case _ => Color.BLACK
      }
    }
  }
}

trait TokenColorizer {
  def toHtml(line: String, theme: ColorizerTheme): String
}
