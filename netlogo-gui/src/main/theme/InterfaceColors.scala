// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.theme

import java.awt.Color

import org.nlogo.api.Constants

object InterfaceColors {
  private var theme: Option[String] = None

  def setTheme(theme: String) {
    this.theme = Some(theme)
  }

  def getTheme = theme.orNull

  private val ClassicLavender = new Color(188, 188, 230)
  private val ClassicLightGreen = new Color(130, 188, 183)
  private val ClassicDarkGreen = new Color(65, 94, 91)
  private val ClassicOrange = new Color(200, 103, 103)
  private val ClassicBeige = new Color(225, 225, 182)

  private val LightBlue = new Color(207, 229, 255)
  private val MediumBlue = new Color(6, 112, 237)
  private val MediumBlue2 = new Color(0, 102, 227)
  private val DarkBlue = new Color(0, 54, 117)
  private val White2 = new Color(245, 245, 245)
  private val LightGray = new Color(238, 238, 238)
  private val LightGray2 = new Color(215, 215, 215)
  private val MediumGray = new Color(175, 175, 175)
  private val LightGrayOutline = new Color(120, 120, 120)
  private val DarkGray = new Color(79, 79, 79)
  private val BlueGray = new Color(70, 70, 76)
  private val MediumBlueGray = new Color(60, 60, 65)
  private val DarkBlueGray = new Color(45, 45, 54)
  private val DarkBlueGray2 = new Color(35, 35, 44)
  private val LightRed = new Color(251, 96, 85)
  private val AlmostBlack = new Color(22, 22, 22)

  val Transparent = new Color(0, 0, 0, 0)

  def widgetText =
    theme.getOrElse("light") match {
      case "classic" => Color.BLACK
      case "light" => new Color(85, 87, 112)
      case "dark" => Color.WHITE
    }

  def widgetTextError = Color.RED

  def widgetHoverShadow = new Color(75, 75, 75)

  def widgetPreviewCover =
    theme.getOrElse("light") match {
      case "classic" | "light" => new Color(255, 255, 255, 150)
      case "dark" => new Color(0, 0, 0, 150)
    }

  def widgetPreviewCoverNote =
    theme.getOrElse("light") match {
      case "classic" | "light" => new Color(225, 225, 225, 150)
      case "dark" => new Color(30, 30, 30, 150)
    }

  def widgetInteractCover =
    theme.getOrElse("light") match {
      case "classic" | "light" => new Color(255, 255, 255, 100)
      case "dark" => new Color(0, 0, 0, 100)
    }

  def widgetInteractCoverNote =
    theme.getOrElse("light") match {
      case "classic" | "light" => new Color(225, 225, 225, 100)
      case "dark" => new Color(30, 30, 30, 100)
    }

  def displayAreaBackground =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.WHITE
      case "dark" => Color.BLACK
    }

  def displayAreaText =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def textBoxBackground =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.WHITE
      case "dark" => AlmostBlack
    }

  def interfaceBackground =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.WHITE
      case "dark" => AlmostBlack
    }

  def commandCenterBackground =
    theme.getOrElse("light") match {
      case "classic" | "light" => LightGray
      case "dark" => BlueGray
    }

  def commandCenterText = widgetText

  def locationToggleImage =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def commandLineBackground =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.WHITE
      case "dark" => DarkGray
    }

  def commandOutputBackground =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.WHITE
      case "dark" => DarkBlueGray
    }

  def splitPaneDividerBackground =
    theme.getOrElse("light") match {
      case "classic" | "light" => MediumGray
      case "dark" => new Color(204, 204, 204)
    }

  def buttonBackground =
    theme.getOrElse("light") match {
      case "classic" => ClassicLavender
      case "light" | "dark" => MediumBlue
    }

  def buttonBackgroundHover =
    theme.getOrElse("light") match {
      case "classic" => ClassicLavender
      case "light" | "dark" => new Color(62, 150, 253)
    }

  def buttonBackgroundPressed =
    theme.getOrElse("light") match {
      case "classic" => Color.BLACK
      case "light" | "dark" => new Color(0, 49, 106)
    }

  def buttonBackgroundPressedHover =
    theme.getOrElse("light") match {
      case "classic" => Color.BLACK
      case "light" | "dark" => new Color(9, 89, 183)
    }

  def buttonBackgroundDisabled =
    theme.getOrElse("light") match {
      case "classic" => ClassicLavender
      case "light" | "dark" => new Color(213, 213, 213)
    }

  def buttonText =
    theme.getOrElse("light") match {
      case "classic" => Color.BLACK
      case "light" | "dark" => Color.WHITE
    }

  def buttonTextPressed =
    theme.getOrElse("light") match {
      case "classic" => ClassicLavender
      case "light" | "dark" => Color.WHITE
    }

  def buttonTextDisabled =
    theme.getOrElse("light") match {
      case "classic" => Color.BLACK
      case "light" | "dark" => new Color(154, 154, 154)
    }

  def sliderBackground =
    theme.getOrElse("light") match {
      case "classic" => ClassicLightGreen
      case "light" => LightBlue
      case "dark" => DarkBlue
    }

  def sliderBarBackground =
    theme.getOrElse("light") match {
      case "classic" => ClassicDarkGreen
      case "light" => MediumGray
      case "dark" => Color.BLACK
    }

  def sliderBarBackgroundFilled =
    theme.getOrElse("light") match {
      case "classic" => ClassicDarkGreen
      case "light" | "dark" => MediumBlue
    }

  def sliderThumbBorder =
    theme.getOrElse("light") match {
      case "classic" => ClassicOrange
      case "light" | "dark" => MediumBlue
    }

  def sliderThumbBackground =
    theme.getOrElse("light") match {
      case "classic" => ClassicOrange
      case "light" | "dark" => Color.WHITE
    }

  def sliderThumbBackgroundPressed =
    theme.getOrElse("light") match {
      case "classic" => ClassicOrange
      case "light" | "dark" => MediumBlue
    }

  def switchBackground =
    theme.getOrElse("light") match {
      case "classic" => ClassicLightGreen
      case "light" => LightBlue
      case "dark" => DarkBlue
    }

  def switchToggle =
    theme.getOrElse("light") match {
      case "classic" => ClassicOrange
      case "light" | "dark" => Color.WHITE
    }

  def switchToggleBackgroundOn =
    theme.getOrElse("light") match {
      case "classic" => ClassicDarkGreen
      case "light" | "dark" => MediumBlue
    }

  def switchToggleBackgroundOff =
    theme.getOrElse("light") match {
      case "classic" => ClassicDarkGreen
      case "light" => MediumGray
      case "dark" => Color.BLACK
    }

  def chooserBackground =
    theme.getOrElse("light") match {
      case "classic" => ClassicLightGreen
      case "light" => LightBlue
      case "dark" => DarkBlue
    }

  def chooserBorder =
    theme.getOrElse("light") match {
      case "classic" => ClassicDarkGreen
      case "light" | "dark" => MediumBlue
    }

  def inputBackground =
    theme.getOrElse("light") match {
      case "classic" => ClassicLightGreen
      case "light" => LightBlue
      case "dark" => DarkBlue
    }

  def inputBorder =
    theme.getOrElse("light") match {
      case "classic" => ClassicDarkGreen
      case "light" | "dark" => MediumBlue
    }

  def graphicsBackground = Constants.ViewBackground

  def viewBorder =
    theme.getOrElse("light") match {
      case "classic" | "light" => Transparent
      case "dark" => LightGrayOutline
    }

  def monitorBackground =
    theme.getOrElse("light") match {
      case "classic" => ClassicBeige
      case "light" => LightGray
      case "dark" => DarkGray
    }

  def monitorBorder =
    theme.getOrElse("light") match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGrayOutline
    }

  def plotBackground =
    theme.getOrElse("light") match {
      case "classic" => ClassicBeige
      case "light" => LightGray
      case "dark" => DarkGray
    }

  def plotBorder =
    theme.getOrElse("light") match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGrayOutline
    }

  def plotMouseBackground =
    theme.getOrElse("light") match {
      case "classic" => ClassicBeige
      case "light" | "dark" => LightGray
    }

  def plotMouseText = Color.BLACK

  def outputBackground =
    theme.getOrElse("light") match {
      case "classic" => ClassicBeige
      case "light" => LightGray
      case "dark" => DarkGray
    }

  def outputBorder =
    theme.getOrElse("light") match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGrayOutline
    }

  def toolbarBackground =
    theme.getOrElse("light") match {
      case "classic" | "light" => LightGray
      case "dark" => BlueGray
    }

  def tabBackground =
    theme.getOrElse("light") match {
      case "classic" | "light" => LightGray
      case "dark" => DarkBlueGray
    }

  def tabBackgroundSelected = MediumBlue

  def tabBackgroundError = LightRed

  def tabText =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def tabTextSelected = Color.WHITE

  def tabTextError = LightRed

  def tabBorder =
    theme.getOrElse("light") match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGrayOutline
    }

  def tabSeparator =
    theme.getOrElse("light") match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGrayOutline
    }

  def toolbarText =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def toolbarControlBackground =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.WHITE
      case "dark" => DarkBlueGray
    }

  def toolbarControlBackgroundHover =
    theme.getOrElse("light") match {
      case "classic" | "light" => White2
      case "dark" => DarkBlueGray2
    }

  def toolbarControlBorder =
    theme.getOrElse("light") match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGrayOutline
    }

  def toolbarButtonPressed =
    theme.getOrElse("light") match {
      case "classic" | "light" => MediumGray
      case "dark" => DarkBlueGray
    }

  def toolbarButtonHover =
    theme.getOrElse("light") match {
      case "classic" | "light" => LightGray2
      case "dark" => MediumBlueGray
    }

  def toolbarToolPressed =
    theme.getOrElse("light") match {
      case "classic" | "light" => MediumGray
      case "dark" => DarkBlueGray2
    }

  def toolbarImage =
    theme.getOrElse("light") match {
      case "classic" | "light" => new Color(85, 87, 112)
      case "dark" => new Color(168, 170, 194)
    }

  def toolbarSeparator =
    theme.getOrElse("light") match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGrayOutline
    }

  def infoBackground =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.WHITE
      case "dark" => AlmostBlack
    }

  def infoH1Background =
    theme.getOrElse("light") match {
      case "classic" | "light" => new Color(209, 208, 255)
      case "dark" => new Color(10, 0, 199)
    }

  def infoH1Color =
    theme.getOrElse("light") match {
      case "classic" | "light" => new Color(19, 13, 134)
      case "dark" => new Color(205, 202, 255)
    }

  def infoH2Background =
    theme.getOrElse("light") match {
      case "classic" | "light" => new Color(211, 231, 255)
      case "dark" => new Color(0, 80, 177)
    }

  def infoH2Color =
    theme.getOrElse("light") match {
      case "classic" | "light" => new Color(0, 90, 200)
      case "dark" => new Color(221, 237, 255)
    }

  def infoH3Color =
    theme.getOrElse("light") match {
      case "classic" | "light" => new Color(88, 88, 88)
      case "dark" => new Color(173, 183, 196)
    }

  def infoH4Color =
    theme.getOrElse("light") match {
      case "classic" | "light" => new Color(115, 115, 115)
      case "dark" => new Color(173, 183, 196)
    }

  def infoPColor =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def infoCodeBackground =
    theme.getOrElse("light") match {
      case "classic" | "light" => LightGray
      case "dark" => new Color(67, 67, 67)
    }

  def infoBlockBar =
    theme.getOrElse("light") match {
      case "classic" | "light" => new Color(96, 96, 96)
      case "dark" => MediumGray
    }

  def infoLink = new Color(0, 110, 240)

  def checkFilled = new Color(0, 173, 90)

  def errorLabelBackground = LightRed

  def errorHighlight = LightRed

  def codeBackground =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.WHITE
      case "dark" => AlmostBlack
    }

  def codeLineHighlight =
    theme.getOrElse("light") match {
      case "classic" | "light" => new Color(255, 255, 204)
      case "dark" => new Color(35, 35, 35)
    }

  def codeSeparator =
    theme.getOrElse("light") match {
      case "classic" | "light" => LightGray
      case "dark" => DarkGray
    }

  def checkboxBackgroundSelected = MediumBlue

  def checkboxBackgroundSelectedHover = MediumBlue2

  def checkboxBackgroundUnselected =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.WHITE
      case "dark" => DarkBlueGray
    }

  def checkboxBackgroundUnselectedHover =
    theme.getOrElse("light") match {
      case "classic"| "light" => White2
      case "dark" => DarkBlueGray2
    }

  def checkboxBorder =
    theme.getOrElse("light") match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGrayOutline
    }

  def checkboxCheck = Color.WHITE

  def menuBarBorder =
    theme.getOrElse("light") match {
      case "classic" | "light" => LightGray2
      case "dark" => MediumBlueGray
    }

  def menuBackground =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.WHITE
      case "dark" => BlueGray
    }

  def menuBackgroundHover =
    theme.getOrElse("light") match {
      case "classic" | "light" => MediumBlue
      case "dark" => DarkBlueGray
    }

  def menuBorder = MediumGray

  def menuTextHover =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.WHITE
      case "dark" => MediumGray
    }

  def menuTextDisabled = MediumGray

  def dialogBackground =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.WHITE
      case "dark" => BlueGray
    }

  def dialogBackgroundSelected =
    theme.getOrElse("light") match {
      case "classic" | "light" => MediumBlue
      case "dark" => DarkBlueGray
    }

  def dialogText =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def dialogTextSelected =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.WHITE
      case "dark" => MediumGray
    }

  def radioButtonBackground =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.WHITE
      case "dark" => DarkBlueGray
    }

  def radioButtonBackgroundHover =
    theme.getOrElse("light") match {
      case "classic" | "light" => White2
      case "dark" => DarkBlueGray2
    }

  def radioButtonSelected = MediumBlue

  def radioButtonSelectedHover = MediumBlue2

  def radioButtonBorder =
    theme.getOrElse("light") match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGrayOutline
    }

  def textAreaBackground =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.WHITE
      case "dark" => DarkBlueGray
    }

  def textAreaText =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def textAreaBorderEditable =
    theme.getOrElse("light") match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGray2
    }

  def textAreaBorderNoneditable =
    theme.getOrElse("light") match {
      case "classic" | "light" => LightGray
      case "dark" => LightGrayOutline
    }

  def tabbedPaneText =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def tabbedPaneTextSelected = Color.WHITE

  def bspaceHintBackground = new Color(128, 200, 128, 64)

  def infoIcon = new Color(50, 150, 200)

  def warningIcon = new Color(220, 170, 50)

  def errorIcon = new Color(220, 50, 50)

  // Syntax highlighting colors

  def commentColor = new Color(90, 90, 90) // gray

  def commandColor =
    theme.getOrElse("light") match {
      case "classic" | "light" => new Color(0, 0, 170) // blue
      case "dark" => new Color(107, 107, 237) // lighter blue
    }

  def reporterColor =
    theme.getOrElse("light") match {
      case "classic" | "light" => new Color(102, 0, 150) // purple
      case "dark" => new Color(151, 71, 255) // lighter purple
    }

  def keywordColor =
    theme.getOrElse("light") match {
      case "classic" | "light" => new Color(0, 127, 105) // bluish green
      case "dark" => new Color(6, 142, 120) // lighter bluish green
    }

  def constantColor =
    theme.getOrElse("light") match {
      case "classic" | "light" => new Color(237, 79, 0) // orange
      case "dark" => new Color(234, 110, 33) // lighter orange
    }

  def defaultColor =
    theme.getOrElse("light") match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }
}

trait ThemeSync {
  def syncTheme(): Unit
}
