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
  case object Classic extends ColorizerTheme {
    override def getColor(tpe: TokenType): Color = {
      tpe match {
        case TokenType.Literal  => new Color(150, 55, 0) // dark orange
        case TokenType.Command  => new Color(0, 0, 170) // blue
        case TokenType.Reporter => new Color(102, 0, 150) // purple
        case TokenType.Keyword  => new Color(0, 127, 105) // bluish green
        case TokenType.Comment  => new Color(120, 120, 120) // medium gray
        case _                  => Color.BLACK
      }
    }
  }

  case object Light extends ColorizerTheme {
    override def getColor(tpe: TokenType): Color = {
      tpe match {
        case TokenType.Literal  => new Color(150, 55, 0) // dark orange
        case TokenType.Command  => new Color(0, 0, 170) // blue
        case TokenType.Reporter => new Color(102, 0, 150) // purple
        case TokenType.Keyword  => new Color(0, 127, 105) // bluish green
        case TokenType.Comment  => new Color(120, 120, 120) // medium gray
        case _                  => Color.BLACK
      }
    }
  }

  case object Dark extends ColorizerTheme {
    override def getColor(tpe: TokenType): Color = {
      tpe match {
        case TokenType.Literal  => new Color(234, 110, 33) // light orange
        case TokenType.Command  => new Color(6, 183, 255) // sky blue
        case TokenType.Reporter => new Color(190, 85, 190) // light purple
        case TokenType.Keyword  => new Color(36, 172, 150) // light bluish green
        case TokenType.Comment  => new Color(150, 150, 150) // light gray
        case _                  => Color.WHITE
      }
    }
  }
}

trait TokenColorizer {
  def toHtml(line: String, theme: ColorizerTheme): String
}
