// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.theme

import java.awt.Color

object InterfaceColors {
  private var theme: Option[String] = None

  def setTheme(theme: String): Unit = {
    this.theme = Option(theme)
  }

  def getTheme = theme.getOrElse("light")

  private val ClassicLavender = new Color(188, 188, 230)
  private val ClassicLightGreen = new Color(130, 188, 183)
  private val ClassicDarkGreen = new Color(65, 94, 91)
  private val ClassicOrange = new Color(200, 103, 103)
  private val ClassicBeige = new Color(225, 225, 182)

  private val LightBlue = new Color(207, 229, 255)
  private val MediumBlue = new Color(6, 112, 237)
  private val MediumBlue2 = new Color(0, 102, 227)
  private val MediumBlue3 = new Color(0, 92, 217)
  private val MediumBlue4 = new Color(0, 72, 197)
  private val DarkBlue = new Color(0, 54, 117)
  private val White2 = new Color(245, 245, 245)
  private val LightGray = new Color(238, 238, 238)
  private val LightGray2 = new Color(215, 215, 215)
  private val MediumGray = new Color(175, 175, 175)
  private val LightGrayOutline = new Color(120, 120, 120)
  private val LightGrayOutline2 = new Color(100, 100, 100)
  private val DarkGray = new Color(79, 79, 79)
  private val BlueGray = new Color(70, 70, 76)
  private val MediumBlueGray = new Color(60, 60, 65)
  private val DarkBlueGray = new Color(45, 45, 54)
  private val DarkBlueGray2 = new Color(35, 35, 44)
  private val DarkBlueGray3 = new Color(25, 25, 34)
  private val LightRed = new Color(251, 96, 85)
  private val AlmostBlack = new Color(22, 22, 22)

  val Transparent = new Color(0, 0, 0, 0)

  def widgetText =
    getTheme match {
      case "classic" => Color.BLACK
      case "light" => new Color(53, 54, 74)
      case "dark" => Color.WHITE
    }

  def widgetTextError = Color.RED

  def widgetHoverShadow = new Color(75, 75, 75)

  def widgetPreviewCover =
    getTheme match {
      case "classic" | "light" => new Color(255, 255, 255, 100)
      case "dark" => new Color(0, 0, 0, 85)
    }

  def widgetPreviewCoverNote =
    getTheme match {
      case "classic" | "light" => new Color(175, 175, 175, 75)
      case "dark" => new Color(100, 100, 100, 75)
    }

  def widgetHandle =
    getTheme match {
      case "classic" | "light" => DarkGray
      case "dark" => MediumGray
    }

  def displayAreaBackground =
    getTheme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => Color.BLACK
    }

  def displayAreaText =
    getTheme match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def textBoxBackground =
    getTheme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => AlmostBlack
    }

  def scrollBarBackground =
    getTheme match {
      case "classic" | "light" => LightGray
      case "dark" => new Color(40, 40, 40)
    }

  def scrollBarForeground =
    getTheme match {
      case "classic" | "light" => MediumGray
      case "dark" => DarkGray
    }

  def scrollBarForegroundHover =
    getTheme match {
      case "classic" | "light" => LightGrayOutline
      case "dark" => LightGrayOutline
    }

  def interfaceBackground =
    getTheme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => AlmostBlack
    }

  def commandCenterBackground =
    getTheme match {
      case "classic" | "light" => LightGray
      case "dark" => BlueGray
    }

  def commandCenterText = widgetText

  def locationToggleImage =
    getTheme match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def commandLineBackground =
    getTheme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => DarkGray
    }

  def commandOutputBackground =
    getTheme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => DarkBlueGray
    }

  def splitPaneDividerBackground =
    getTheme match {
      case "classic" | "light" => MediumGray
      case "dark" => new Color(204, 204, 204)
    }

  def speedSliderBarBackground = MediumGray

  def speedSliderBarBackgroundFilled = MediumBlue

  def speedSliderThumb = MediumBlue

  def speedSliderThumbDisabled = MediumGray

  def buttonBackground =
    getTheme match {
      case "classic" => ClassicLavender
      case "light" | "dark" => MediumBlue
    }

  def buttonBackgroundHover =
    getTheme match {
      case "classic" => ClassicLavender
      case "light" | "dark" => new Color(62, 150, 253)
    }

  def buttonBackgroundPressed =
    getTheme match {
      case "classic" => Color.BLACK
      case "light" => new Color(5, 69, 143)
      case "dark" => new Color(9, 89, 183)
    }

  def buttonBackgroundPressedHover =
    getTheme match {
      case "classic" => Color.BLACK
      case "light" | "dark" => new Color(0, 49, 106)
    }

  def buttonBackgroundDisabled =
    getTheme match {
      case "classic" => ClassicLavender
      case "light" | "dark" => new Color(213, 213, 213)
    }

  def buttonText =
    getTheme match {
      case "classic" => Color.BLACK
      case "light" | "dark" => Color.WHITE
    }

  def buttonTextPressed =
    getTheme match {
      case "classic" => ClassicLavender
      case "light" | "dark" => Color.WHITE
    }

  def buttonTextDisabled =
    getTheme match {
      case "classic" => Color.BLACK
      case "light" | "dark" => new Color(154, 154, 154)
    }

  def sliderBackground =
    getTheme match {
      case "classic" => ClassicLightGreen
      case "light" => LightBlue
      case "dark" => DarkBlue
    }

  def sliderBarBackground =
    getTheme match {
      case "classic" => ClassicDarkGreen
      case "light" => MediumGray
      case "dark" => Color.BLACK
    }

  def sliderBarBackgroundFilled =
    getTheme match {
      case "classic" => ClassicDarkGreen
      case "light" | "dark" => MediumBlue
    }

  def sliderThumbBorder =
    getTheme match {
      case "classic" => ClassicOrange
      case "light" | "dark" => MediumBlue
    }

  def sliderThumbBackground =
    getTheme match {
      case "classic" => ClassicOrange
      case "light" | "dark" => Color.WHITE
    }

  def sliderThumbBackgroundPressed =
    getTheme match {
      case "classic" => ClassicOrange
      case "light" | "dark" => MediumBlue
    }

  def switchBackground =
    getTheme match {
      case "classic" => ClassicLightGreen
      case "light" => LightBlue
      case "dark" => DarkBlue
    }

  def switchToggle =
    getTheme match {
      case "classic" => ClassicOrange
      case "light" | "dark" => Color.WHITE
    }

  def switchToggleBackgroundOn =
    getTheme match {
      case "classic" => ClassicDarkGreen
      case "light" | "dark" => MediumBlue
    }

  def switchToggleBackgroundOff =
    getTheme match {
      case "classic" => ClassicDarkGreen
      case "light" => MediumGray
      case "dark" => Color.BLACK
    }

  def chooserBackground =
    getTheme match {
      case "classic" => ClassicLightGreen
      case "light" => LightBlue
      case "dark" => DarkBlue
    }

  def chooserBorder =
    getTheme match {
      case "classic" => ClassicDarkGreen
      case "light" | "dark" => MediumBlue
    }

  def inputBackground =
    getTheme match {
      case "classic" => ClassicLightGreen
      case "light" => LightBlue
      case "dark" => DarkBlue
    }

  def inputBorder =
    getTheme match {
      case "classic" => ClassicDarkGreen
      case "light" | "dark" => MediumBlue
    }

  def viewBackground =
    getTheme match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGrayOutline2
    }

  def viewBorder =
    getTheme match {
      case "classic" | "light" => Color.BLACK
      case "dark" => LightGrayOutline
    }

  def monitorBackground =
    getTheme match {
      case "classic" => ClassicBeige
      case "light" => LightGray
      case "dark" => DarkGray
    }

  def monitorBorder =
    getTheme match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGrayOutline
    }

  def plotBackground =
    getTheme match {
      case "classic" => ClassicBeige
      case "light" => LightGray
      case "dark" => DarkGray
    }

  def plotBorder =
    getTheme match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGrayOutline
    }

  def plotMouseBackground =
    getTheme match {
      case "classic" => ClassicBeige
      case "light" | "dark" => LightGray
    }

  def plotMouseText = Color.BLACK

  def outputBackground =
    getTheme match {
      case "classic" => ClassicBeige
      case "light" => LightGray
      case "dark" => DarkGray
    }

  def outputBorder =
    getTheme match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGrayOutline
    }

  def toolbarBackground =
    getTheme match {
      case "classic" | "light" => LightGray
      case "dark" => BlueGray
    }

  def tabBackground =
    getTheme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => DarkBlueGray
    }

  def tabBackgroundHover =
    getTheme match {
      case "classic" | "light" => LightGray
      case "dark" => DarkBlueGray2
    }

  def tabBackgroundSelected = MediumBlue

  def tabBackgroundError = LightRed

  def tabText =
    getTheme match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def tabTextSelected = Color.WHITE

  def tabTextError = LightRed

  def tabBorder =
    getTheme match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGrayOutline
    }

  def tabSeparator =
    getTheme match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGrayOutline
    }

  def toolbarText =
    getTheme match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def toolbarTextSelected = Color.WHITE

  def toolbarControlBackground =
    getTheme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => DarkBlueGray
    }

  def toolbarControlBackgroundHover =
    getTheme match {
      case "classic" | "light" => White2
      case "dark" => DarkBlueGray2
    }

  def toolbarControlBackgroundPressed =
    getTheme match {
      case "classic" | "light" => LightGray2
      case "dark" => DarkBlueGray3
    }

  def toolbarControlBorder =
    getTheme match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGrayOutline
    }

  def toolbarControlBorderSelected =
    getTheme match {
      case "classic" | "light" => Transparent
      case "dark" => LightGrayOutline
    }

  def toolbarControlFocus =
    getTheme match {
      case "classic" | "light" => MediumBlue
      case "dark" => Color.WHITE
    }

  def toolbarButtonHover =
    getTheme match {
      case "classic" | "light" => LightGray2
      case "dark" => MediumBlueGray
    }

  def toolbarToolPressed =
    getTheme match {
      case "classic" | "light" => MediumGray
      case "dark" => DarkBlueGray2
    }

  def toolbarToolSelected =
    getTheme match {
      case "classic" | "light" => MediumBlue
      case "dark" => MediumBlue2
    }

  def toolbarImage =
    getTheme match {
      case "classic" | "light" => new Color(85, 87, 112)
      case "dark" => new Color(168, 170, 194)
    }

  def toolbarImageSelected =
    getTheme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => LightGray2
    }

  def toolbarImageDisabled =
    getTheme match {
      case "classic" | "light" => new Color(100, 100, 100, 64)
      case "dark" => new Color(150, 150, 150, 64)
    }

  def toolbarSeparator =
    getTheme match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGrayOutline
    }

  def infoBackground =
    getTheme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => AlmostBlack
    }

  def infoH1Background =
    getTheme match {
      case "classic" | "light" => new Color(209, 208, 255)
      case "dark" => new Color(10, 0, 199)
    }

  def infoH1Color =
    getTheme match {
      case "classic" | "light" => new Color(19, 13, 134)
      case "dark" => new Color(205, 202, 255)
    }

  def infoH2Background =
    getTheme match {
      case "classic" | "light" => new Color(211, 231, 255)
      case "dark" => new Color(0, 80, 177)
    }

  def infoH2Color =
    getTheme match {
      case "classic" | "light" => new Color(0, 90, 200)
      case "dark" => new Color(221, 237, 255)
    }

  def infoH3Color =
    getTheme match {
      case "classic" | "light" => new Color(88, 88, 88)
      case "dark" => new Color(173, 183, 196)
    }

  def infoH4Color =
    getTheme match {
      case "classic" | "light" => new Color(115, 115, 115)
      case "dark" => new Color(173, 183, 196)
    }

  def infoPColor =
    getTheme match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def infoCodeBackground =
    getTheme match {
      case "classic" | "light" => LightGray
      case "dark" => new Color(67, 67, 67)
    }

  def infoBlockBar =
    getTheme match {
      case "classic" | "light" => new Color(96, 96, 96)
      case "dark" => MediumGray
    }

  def infoLink = new Color(0, 110, 240)

  def checkFilled = new Color(0, 173, 90)

  def errorLabelText = Color.WHITE

  def errorLabelBackground = LightRed

  def errorHighlight = LightRed

  def codeBackground =
    getTheme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => AlmostBlack
    }

  def codeLineHighlight =
    getTheme match {
      case "classic" | "light" => new Color(255, 255, 204)
      case "dark" => new Color(35, 35, 35)
    }

  def codeSeparator =
    getTheme match {
      case "classic" | "light" => LightGray
      case "dark" => DarkGray
    }

  def checkboxBackgroundSelected = MediumBlue

  def checkboxBackgroundSelectedHover = MediumBlue2

  def checkboxBackgroundUnselected =
    getTheme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => DarkBlueGray
    }

  def checkboxBackgroundUnselectedHover =
    getTheme match {
      case "classic" | "light" => White2
      case "dark" => DarkBlueGray2
    }

  def checkboxBackgroundDisabled =
    getTheme match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGrayOutline
    }

  def checkboxBorder =
    getTheme match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGrayOutline
    }

  def checkboxCheck = Color.WHITE

  def menuBarBorder =
    getTheme match {
      case "classic" | "light" => LightGray2
      case "dark" => MediumBlueGray
    }

  def menuBackground =
    getTheme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => BlueGray
    }

  def menuBackgroundHover =
    getTheme match {
      case "classic" | "light" => MediumBlue
      case "dark" => new Color(6, 112, 237, 128)
    }

  def menuBorder = MediumGray

  def menuTextHover = Color.WHITE

  def menuTextDisabled = MediumGray

  def dialogBackground =
    getTheme match {
      case "classic" | "light" => White2
      case "dark" => BlueGray
    }

  def dialogBackgroundSelected =
    getTheme match {
      case "classic" | "light" => MediumBlue
      case "dark" => new Color(6, 112, 237, 128)
    }

  def dialogText =
    getTheme match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def dialogTextSelected = Color.WHITE

  def radioButtonBackground =
    getTheme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => DarkBlueGray
    }

  def radioButtonBackgroundHover =
    getTheme match {
      case "classic" | "light" => White2
      case "dark" => DarkBlueGray2
    }

  def radioButtonSelected = MediumBlue

  def radioButtonSelectedHover = MediumBlue2

  def radioButtonBorder =
    getTheme match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGrayOutline
    }

  def primaryButtonBackground = MediumBlue

  def primaryButtonBackgroundHover = MediumBlue3

  def primaryButtonBackgroundPressed = MediumBlue4

  def primaryButtonBorder = MediumBlue

  def primaryButtonText = Color.WHITE

  def primaryButtonFocus =
    getTheme match {
      case "classic" | "light" => MediumBlue
      case "dark" => Color.WHITE
    }

  def secondaryButtonBackground =
    getTheme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => DarkBlueGray
    }

  def secondaryButtonBackgroundHover =
    getTheme match {
      case "classic" | "light" => White2
      case "dark" => DarkBlueGray2
    }

  def secondaryButtonBackgroundPressed =
    getTheme match {
      case "classic" | "light" => LightGray2
      case "dark" => DarkBlueGray3
    }

  def secondaryButtonBorder = MediumGray

  def secondaryButtonText =
    getTheme match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def secondaryButtonFocus =
    getTheme match {
      case "classic" | "light" => MediumBlue
      case "dark" => Color.WHITE
    }

  def textAreaBackground =
    getTheme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => DarkBlueGray
    }

  def textAreaText =
    getTheme match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def textAreaBorderEditable =
    getTheme match {
      case "classic" | "light" => MediumGray
      case "dark" => LightGray2
    }

  def textAreaBorderNoneditable =
    getTheme match {
      case "classic" | "light" => LightGray
      case "dark" => LightGrayOutline
    }

  def tabbedPaneText =
    getTheme match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def tabbedPaneTextSelected = Color.WHITE

  def bspaceHintBackground = new Color(128, 200, 128, 64)

  def infoIcon = new Color(50, 150, 200)

  def warningIcon = new Color(220, 170, 50)

  def errorIcon = new Color(220, 50, 50)

  def updateIcon = new Color(240, 91, 0)

  def stockBackground = ClassicBeige

  def converterBackground = ClassicLightGreen

  // Syntax highlighting colors

  def commentColor =
    getTheme match {
      case "classic" | "light" => new Color(120, 120, 120) // medium gray
      case "dark" => new Color(150, 150, 150) // light gray
    }

  def commandColor =
    getTheme match {
      case "classic" | "light" => new Color(0, 0, 170) // blue
      case "dark" => new Color(6, 183, 255) // sky blue
    }

  def reporterColor =
    getTheme match {
      case "classic" | "light" => new Color(102, 0, 150) // purple
      case "dark" => new Color(190, 85, 190) // light purple
    }

  def keywordColor =
    getTheme match {
      case "classic" | "light" => new Color(0, 127, 105) // bluish green
      case "dark" => new Color(36, 172, 150) // light bluish green
    }

  def constantColor =
    getTheme match {
      case "classic" | "light" => new Color(150, 55, 0) // dark orange
      case "dark" => new Color(234, 110, 33) // light orange
    }

  def defaultColor =
    getTheme match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def announceX() =
    getTheme match {
      case "classic" | "light" => DarkGray
      case "dark"              => White2
    }

  def announceXHovered() =
    getTheme match {
      case "classic" | "light" => LightGrayOutline
      case "dark"              => MediumGray
    }

  def announceXPressed() =
    getTheme match {
      case "classic" | "light" => MediumGray
      case "dark"              => DarkGray
    }

  def announceRelease() =
    getTheme match {
      case "classic" | "light" => new Color(237, 205, 255)
      case "dark"              => new Color(137, 105, 155)
    }

  def announceAdvisory() =
    getTheme match {
      case "classic" | "light" => new Color(255, 194, 154)
      case "dark"              => new Color(155,  94,  54)
    }

  def announceEvent() =
    getTheme match {
      case "classic" | "light" => new Color(108, 252, 221)
      case "dark"              => new Color(  8, 152, 121)
    }

}

trait ThemeSync {
  def syncTheme(): Unit
}
