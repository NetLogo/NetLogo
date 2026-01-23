// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.theme

import java.awt.Color

import org.nlogo.core.{ ColorizerTheme, TokenType }

object InterfaceColors {
  private var theme: ColorTheme = LightTheme

  def setTheme(theme: ColorTheme): Unit = {
    this.theme = theme
  }

  def getTheme: ColorTheme =
    theme

  val Transparent = new Color(0, 0, 0, 0)

  def widgetText(): Color = theme.widgetText
  def widgetTextError(): Color = theme.widgetTextError
  def widgetHoverShadow(): Color = theme.widgetHoverShadow
  def widgetPreviewCover(): Color = theme.widgetPreviewCover
  def widgetPreviewCoverNote(): Color = theme.widgetPreviewCoverNote
  def widgetHandle(): Color = theme.widgetHandle
  def displayAreaBackground(): Color = theme.displayAreaBackground
  def displayAreaText(): Color = theme.displayAreaText
  def textBoxBackground(): Color = theme.textBoxBackground
  def scrollBarBackground(): Color = theme.scrollBarBackground
  def scrollBarForeground(): Color = theme.scrollBarForeground
  def scrollBarForegroundHover(): Color = theme.scrollBarForegroundHover
  def interfaceBackground(): Color = theme.interfaceBackground
  def commandCenterBackground(): Color = theme.commandCenterBackground
  def commandCenterText(): Color = theme.commandCenterText
  def locationToggleImage(): Color = theme.locationToggleImage
  def commandLineBackground(): Color = theme.commandLineBackground
  def commandOutputBackground(): Color = theme.commandOutputBackground
  def splitPaneDividerBackground(): Color = theme.splitPaneDividerBackground
  def speedSliderBarBackground(): Color = theme.speedSliderBarBackground
  def speedSliderBarBackgroundFilled(): Color = theme.speedSliderBarBackgroundFilled
  def speedSliderThumb(): Color = theme.speedSliderThumb
  def speedSliderThumbDisabled(): Color = theme.speedSliderThumbDisabled
  def buttonBackground(): Color = theme.buttonBackground
  def buttonBackgroundHover(): Color = theme.buttonBackgroundHover
  def buttonBackgroundPressed(): Color = theme.buttonBackgroundPressed
  def buttonBackgroundPressedHover(): Color = theme.buttonBackgroundPressedHover
  def buttonBackgroundDisabled(): Color = theme.buttonBackgroundDisabled
  def buttonText(): Color = theme.buttonText
  def buttonTextPressed(): Color = theme.buttonTextPressed
  def buttonTextDisabled(): Color = theme.buttonTextDisabled
  def sliderBackground(): Color = theme.sliderBackground
  def sliderBarBackground(): Color = theme.sliderBarBackground
  def sliderBarBackgroundFilled(): Color = theme.sliderBarBackgroundFilled
  def sliderThumbBorder(): Color = theme.sliderThumbBorder
  def sliderThumbBackground(): Color = theme.sliderThumbBackground
  def sliderThumbBackgroundPressed(): Color = theme.sliderThumbBackgroundPressed
  def switchBackground(): Color = theme.switchBackground
  def switchToggle(): Color = theme.switchToggle
  def switchToggleBackgroundOn(): Color = theme.switchToggleBackgroundOn
  def switchToggleBackgroundOff(): Color = theme.switchToggleBackgroundOff
  def chooserBackground(): Color = theme.chooserBackground
  def chooserBorder(): Color = theme.chooserBorder
  def inputBackground(): Color = theme.inputBackground
  def inputBorder(): Color = theme.inputBorder
  def viewBackground(): Color = theme.viewBackground
  def viewBorder(): Color = theme.viewBorder
  def monitorBackground(): Color = theme.monitorBackground
  def monitorBorder(): Color = theme.monitorBorder
  def plotBackground(): Color = theme.plotBackground
  def plotBorder(): Color = theme.plotBorder
  def plotMouseBackground(): Color = theme.plotMouseBackground
  def plotMouseText(): Color = theme.plotMouseText
  def outputBackground(): Color = theme.outputBackground
  def outputBorder(): Color = theme.outputBorder
  def toolbarBackground(): Color = theme.toolbarBackground
  def tabBackground(): Color = theme.tabBackground
  def tabBackgroundHover(): Color = theme.tabBackgroundHover
  def tabBackgroundSelected(): Color = theme.tabBackgroundSelected
  def tabBackgroundError(): Color = theme.tabBackgroundError
  def tabText(): Color = theme.tabText
  def tabTextSelected(): Color = theme.tabTextSelected
  def tabTextError(): Color = theme.tabTextError
  def tabBorder(): Color = theme.tabBorder
  def tabSeparator(): Color = theme.tabSeparator
  def tabCloseButtonBackgroundHover(): Color = theme.tabCloseButtonBackgroundHover
  def toolbarText(): Color = theme.toolbarText
  def toolbarTextSelected(): Color = theme.toolbarTextSelected
  def toolbarControlBackground(): Color = theme.toolbarControlBackground
  def toolbarControlBackgroundHover(): Color = theme.toolbarControlBackgroundHover
  def toolbarControlBackgroundPressed(): Color = theme.toolbarControlBackgroundPressed
  def toolbarControlBorder(): Color = theme.toolbarControlBorder
  def toolbarControlBorderSelected(): Color = theme.toolbarControlBorderSelected
  def toolbarControlFocus(): Color = theme.toolbarControlFocus
  def toolbarButtonHover(): Color = theme.toolbarButtonHover
  def toolbarToolPressed(): Color = theme.toolbarToolPressed
  def toolbarToolSelected(): Color = theme.toolbarToolSelected
  def toolbarImage(): Color = theme.toolbarImage
  def toolbarImageSelected(): Color = theme.toolbarImageSelected
  def toolbarImageDisabled(): Color = theme.toolbarImageDisabled
  def toolbarSeparator(): Color = theme.toolbarSeparator
  def infoBackground(): Color = theme.infoBackground
  def infoH1Background(): Color = theme.infoH1Background
  def infoH1Color(): Color = theme.infoH1Color
  def infoH2Background(): Color = theme.infoH2Background
  def infoH2Color(): Color = theme.infoH2Color
  def infoH3Color(): Color = theme.infoH3Color
  def infoH4Color(): Color = theme.infoH4Color
  def infoPColor(): Color = theme.infoPColor
  def infoCodeBackground(): Color = theme.infoCodeBackground
  def infoCodeText(): Color = theme.infoCodeText
  def infoBlockBar(): Color = theme.infoBlockBar
  def infoLink(): Color = theme.infoLink
  def checkFilled(): Color = theme.checkFilled
  def errorLabelText(): Color = theme.errorLabelText
  def errorLabelBackground(): Color = theme.errorLabelBackground
  def warningLabelText(): Color = theme.warningLabelText
  def warningLabelBackground(): Color = theme.warningLabelBackground
  def errorHighlight(): Color = theme.errorHighlight
  def codeBackground(): Color = theme.codeBackground
  def codeLineHighlight(): Color = theme.codeLineHighlight
  def codeBracketHighlight(): Color = theme.codeBracketHighlight
  def codeSelection(): Color = theme.codeSelection
  def codeSeparator(): Color = theme.codeSeparator
  def checkboxBackgroundSelected(): Color = theme.checkboxBackgroundSelected
  def checkboxBackgroundSelectedHover(): Color = theme.checkboxBackgroundSelectedHover
  def checkboxBackgroundUnselected(): Color = theme.checkboxBackgroundUnselected
  def checkboxBackgroundUnselectedHover(): Color = theme.checkboxBackgroundUnselectedHover
  def checkboxBackgroundDisabled(): Color = theme.checkboxBackgroundDisabled
  def checkboxBorder(): Color = theme.checkboxBorder
  def checkboxCheck(): Color = theme.checkboxCheck
  def menuBarBorder(): Color = theme.menuBarBorder
  def menuBackground(): Color = theme.menuBackground
  def menuBackgroundHover(): Color = theme.menuBackgroundHover
  def menuBorder(): Color = theme.menuBorder
  def menuTextHover(): Color = theme.menuTextHover
  def menuTextDisabled(): Color = theme.menuTextDisabled
  def dialogBackground(): Color = theme.dialogBackground
  def dialogBackgroundSelected(): Color = theme.dialogBackgroundSelected
  def dialogText(): Color = theme.dialogText
  def dialogTextSelected(): Color = theme.dialogTextSelected
  def radioButtonBackground(): Color = theme.radioButtonBackground
  def radioButtonBackgroundHover(): Color = theme.radioButtonBackgroundHover
  def radioButtonSelected(): Color = theme.radioButtonSelected
  def radioButtonSelectedHover(): Color = theme.radioButtonSelectedHover
  def radioButtonBorder(): Color = theme.radioButtonBorder
  def primaryButtonBackground(): Color = theme.primaryButtonBackground
  def primaryButtonBackgroundHover(): Color = theme.primaryButtonBackgroundHover
  def primaryButtonBackgroundPressed(): Color = theme.primaryButtonBackgroundPressed
  def primaryButtonBorder(): Color = theme.primaryButtonBorder
  def primaryButtonText(): Color = theme.primaryButtonText
  def secondaryButtonBackground(): Color = theme.secondaryButtonBackground
  def secondaryButtonBackgroundHover(): Color = theme.secondaryButtonBackgroundHover
  def secondaryButtonBackgroundPressed(): Color = theme.secondaryButtonBackgroundPressed
  def secondaryButtonBorder(): Color = theme.secondaryButtonBorder
  def secondaryButtonText(): Color = theme.secondaryButtonText
  def textAreaBackground(): Color = theme.textAreaBackground
  def textAreaText(): Color = theme.textAreaText
  def textAreaBorderEditable(): Color = theme.textAreaBorderEditable
  def textAreaBorderNoneditable(): Color = theme.textAreaBorderNoneditable
  def tabbedPaneText(): Color = theme.tabbedPaneText
  def tabbedPaneTextSelected(): Color = theme.tabbedPaneTextSelected
  def bspaceHintBackground(): Color = theme.bspaceHintBackground
  def infoIcon(): Color = theme.infoIcon
  def warningIcon(): Color = theme.warningIcon
  def errorIcon(): Color = theme.errorIcon
  def updateIcon(): Color = theme.updateIcon
  def stockBackground(): Color = theme.stockBackground
  def converterBackground(): Color = theme.converterBackground
  def commentColor(): Color = theme.colorizerTheme.getColor(TokenType.Comment)
  def commandColor(): Color = theme.colorizerTheme.getColor(TokenType.Command)
  def reporterColor(): Color = theme.colorizerTheme.getColor(TokenType.Reporter)
  def keywordColor(): Color = theme.colorizerTheme.getColor(TokenType.Keyword)
  def constantColor(): Color = theme.colorizerTheme.getColor(TokenType.Literal)
  def defaultColor(): Color = theme.colorizerTheme.getColor(null)
  def announceX(): Color = theme.announceX
  def announceXHovered(): Color = theme.announceXHovered
  def announceXPressed(): Color = theme.announceXPressed
  def announceRelease(): Color = theme.announceRelease
  def announceAdvisory(): Color = theme.announceAdvisory
  def announceEvent(): Color = theme.announceEvent
  def colorPickerOutputBackground(): Color = theme.colorPickerOutputBackground
  def colorPickerCheckmark(): Color = theme.colorPickerCheckmark
  def colorPickerCopyHover(): Color = theme.colorPickerCopyHover
  def agentMonitorSeparator(): Color = theme.agentMonitorSeparator
}

trait ColorTheme {
  protected val ClassicLavender = new Color(188, 188, 230)
  protected val ClassicLightGreen = new Color(130, 188, 183)
  protected val ClassicDarkGreen = new Color(65, 94, 91)
  protected val ClassicOrange = new Color(200, 103, 103)
  protected val ClassicBeige = new Color(225, 225, 182)

  protected val LightBlue = new Color(207, 229, 255)
  protected val MediumBlue = new Color(6, 112, 237)
  protected val MediumBlue2 = new Color(0, 102, 227)
  protected val MediumBlue3 = new Color(0, 92, 217)
  protected val MediumBlue4 = new Color(0, 72, 197)
  protected val DarkBlue = new Color(0, 54, 117)
  protected val White2 = new Color(245, 245, 245)
  protected val LightGray = new Color(238, 238, 238)
  protected val LightGray1 = new Color(232, 232, 232)
  protected val LightGray2 = new Color(215, 215, 215)
  protected val MediumGray = new Color(175, 175, 175)
  protected val LightGrayOutline = new Color(120, 120, 120)
  protected val LightGrayOutline2 = new Color(100, 100, 100)
  protected val DarkGray = new Color(79, 79, 79)
  protected val BlueGray = new Color(70, 70, 76)
  protected val MediumBlueGray = new Color(60, 60, 65)
  protected val DarkBlueGray = new Color(45, 45, 54)
  protected val DarkBlueGray2 = new Color(35, 35, 44)
  protected val DarkBlueGray3 = new Color(25, 25, 34)
  protected val DarkBlueGray4 = new Color(10, 10, 20)
  protected val LightRed = new Color(251, 96, 85)
  protected val AlmostBlack = new Color(22, 22, 22)

  def widgetText: Color
  def widgetTextError: Color
  def widgetHoverShadow: Color
  def widgetPreviewCover: Color
  def widgetPreviewCoverNote: Color
  def widgetHandle: Color
  def displayAreaBackground: Color
  def displayAreaText: Color
  def textBoxBackground: Color
  def scrollBarBackground: Color
  def scrollBarForeground: Color
  def scrollBarForegroundHover: Color
  def interfaceBackground: Color
  def commandCenterBackground: Color
  def commandCenterText: Color
  def locationToggleImage: Color
  def commandLineBackground: Color
  def commandOutputBackground: Color
  def splitPaneDividerBackground: Color
  def speedSliderBarBackground: Color
  def speedSliderBarBackgroundFilled: Color
  def speedSliderThumb: Color
  def speedSliderThumbDisabled: Color
  def buttonBackground: Color
  def buttonBackgroundHover: Color
  def buttonBackgroundPressed: Color
  def buttonBackgroundPressedHover: Color
  def buttonBackgroundDisabled: Color
  def buttonText: Color
  def buttonTextPressed: Color
  def buttonTextDisabled: Color
  def sliderBackground: Color
  def sliderBarBackground: Color
  def sliderBarBackgroundFilled: Color
  def sliderThumbBorder: Color
  def sliderThumbBackground: Color
  def sliderThumbBackgroundPressed: Color
  def switchBackground: Color
  def switchToggle: Color
  def switchToggleBackgroundOn: Color
  def switchToggleBackgroundOff: Color
  def chooserBackground: Color
  def chooserBorder: Color
  def inputBackground: Color
  def inputBorder: Color
  def viewBackground: Color
  def viewBorder: Color
  def monitorBackground: Color
  def monitorBorder: Color
  def plotBackground: Color
  def plotBorder: Color
  def plotMouseBackground: Color
  def plotMouseText: Color
  def outputBackground: Color
  def outputBorder: Color
  def toolbarBackground: Color
  def tabBackground: Color
  def tabBackgroundHover: Color
  def tabBackgroundSelected: Color
  def tabBackgroundError: Color
  def tabText: Color
  def tabTextSelected: Color
  def tabTextError: Color
  def tabBorder: Color
  def tabSeparator: Color
  def tabCloseButtonBackgroundHover: Color
  def toolbarText: Color
  def toolbarTextSelected: Color
  def toolbarControlBackground: Color
  def toolbarControlBackgroundHover: Color
  def toolbarControlBackgroundPressed: Color
  def toolbarControlBorder: Color
  def toolbarControlBorderSelected: Color
  def toolbarControlFocus: Color
  def toolbarButtonHover: Color
  def toolbarToolPressed: Color
  def toolbarToolSelected: Color
  def toolbarImage: Color
  def toolbarImageSelected: Color
  def toolbarImageDisabled: Color
  def toolbarSeparator: Color
  def infoBackground: Color
  def infoH1Background: Color
  def infoH1Color: Color
  def infoH2Background: Color
  def infoH2Color: Color
  def infoH3Color: Color
  def infoH4Color: Color
  def infoPColor: Color
  def infoCodeBackground: Color
  def infoCodeText: Color
  def infoBlockBar: Color
  def infoLink: Color
  def checkFilled: Color
  def errorLabelText: Color
  def errorLabelBackground: Color
  def warningLabelText: Color
  def warningLabelBackground: Color
  def errorHighlight: Color
  def codeBackground: Color
  def codeLineHighlight: Color
  def codeBracketHighlight: Color
  def codeSelection: Color
  def codeSeparator: Color
  def checkboxBackgroundSelected: Color
  def checkboxBackgroundSelectedHover: Color
  def checkboxBackgroundUnselected: Color
  def checkboxBackgroundUnselectedHover: Color
  def checkboxBackgroundDisabled: Color
  def checkboxBorder: Color
  def checkboxCheck: Color
  def menuBarBorder: Color
  def menuBackground: Color
  def menuBackgroundHover: Color
  def menuBorder: Color
  def menuTextHover: Color
  def menuTextDisabled: Color
  def dialogBackground: Color
  def dialogBackgroundSelected: Color
  def dialogText: Color
  def dialogTextSelected: Color
  def radioButtonBackground: Color
  def radioButtonBackgroundHover: Color
  def radioButtonSelected: Color
  def radioButtonSelectedHover: Color
  def radioButtonBorder: Color
  def primaryButtonBackground: Color
  def primaryButtonBackgroundHover: Color
  def primaryButtonBackgroundPressed: Color
  def primaryButtonBorder: Color
  def primaryButtonText: Color
  def secondaryButtonBackground: Color
  def secondaryButtonBackgroundHover: Color
  def secondaryButtonBackgroundPressed: Color
  def secondaryButtonBorder: Color
  def secondaryButtonText: Color
  def textAreaBackground: Color
  def textAreaText: Color
  def textAreaBorderEditable: Color
  def textAreaBorderNoneditable: Color
  def tabbedPaneText: Color
  def tabbedPaneTextSelected: Color
  def bspaceHintBackground: Color
  def infoIcon: Color
  def warningIcon: Color
  def errorIcon: Color
  def updateIcon: Color
  def stockBackground: Color
  def converterBackground: Color
  def announceX: Color
  def announceXHovered: Color
  def announceXPressed: Color
  def announceRelease: Color
  def announceAdvisory: Color
  def announceEvent: Color
  def colorPickerOutputBackground: Color
  def colorPickerCheckmark: Color
  def colorPickerCopyHover: Color
  def agentMonitorSeparator: Color

  def colorizerTheme: ColorizerTheme
}

object ClassicTheme extends ColorTheme {
  override def widgetText: Color = Color.BLACK
  override def widgetTextError: Color = LightRed
  override def widgetHoverShadow: Color = new Color(75, 75, 75)
  override def widgetPreviewCover: Color = new Color(255, 255, 255, 100)
  override def widgetPreviewCoverNote: Color = new Color(175, 175, 175, 75)
  override def widgetHandle: Color = DarkGray
  override def displayAreaBackground: Color = Color.WHITE
  override def displayAreaText: Color = Color.BLACK
  override def textBoxBackground: Color = Color.WHITE
  override def scrollBarBackground: Color = LightGray
  override def scrollBarForeground: Color = MediumGray
  override def scrollBarForegroundHover: Color = LightGrayOutline
  override def interfaceBackground: Color = Color.WHITE
  override def commandCenterBackground: Color = LightGray
  override def commandCenterText: Color = Color.BLACK
  override def locationToggleImage: Color = Color.BLACK
  override def commandLineBackground: Color = Color.WHITE
  override def commandOutputBackground: Color = Color.WHITE
  override def splitPaneDividerBackground: Color = MediumGray
  override def speedSliderBarBackground: Color = MediumGray
  override def speedSliderBarBackgroundFilled: Color = MediumBlue
  override def speedSliderThumb: Color = MediumBlue
  override def speedSliderThumbDisabled: Color = MediumGray
  override def buttonBackground: Color = ClassicLavender
  override def buttonBackgroundHover: Color = ClassicLavender
  override def buttonBackgroundPressed: Color = Color.BLACK
  override def buttonBackgroundPressedHover: Color = Color.BLACK
  override def buttonBackgroundDisabled: Color = ClassicLavender
  override def buttonText: Color = Color.BLACK
  override def buttonTextPressed: Color = ClassicLavender
  override def buttonTextDisabled: Color = Color.BLACK
  override def sliderBackground: Color = ClassicLightGreen
  override def sliderBarBackground: Color = ClassicDarkGreen
  override def sliderBarBackgroundFilled: Color = ClassicDarkGreen
  override def sliderThumbBorder: Color = ClassicOrange
  override def sliderThumbBackground: Color = ClassicOrange
  override def sliderThumbBackgroundPressed: Color = ClassicOrange
  override def switchBackground: Color = ClassicLightGreen
  override def switchToggle: Color = ClassicOrange
  override def switchToggleBackgroundOn: Color = ClassicDarkGreen
  override def switchToggleBackgroundOff: Color = ClassicDarkGreen
  override def chooserBackground: Color = ClassicLightGreen
  override def chooserBorder: Color = ClassicDarkGreen
  override def inputBackground: Color = ClassicLightGreen
  override def inputBorder: Color = ClassicDarkGreen
  override def viewBackground: Color = MediumGray
  override def viewBorder: Color = Color.BLACK
  override def monitorBackground: Color = ClassicBeige
  override def monitorBorder: Color = MediumGray
  override def plotBackground: Color = ClassicBeige
  override def plotBorder: Color = MediumGray
  override def plotMouseBackground: Color = ClassicBeige
  override def plotMouseText: Color = Color.BLACK
  override def outputBackground: Color = ClassicBeige
  override def outputBorder: Color = MediumGray
  override def toolbarBackground: Color = LightGray
  override def tabBackground: Color = Color.WHITE
  override def tabBackgroundHover: Color = LightGray
  override def tabBackgroundSelected: Color = MediumBlue
  override def tabBackgroundError: Color = LightRed
  override def tabText: Color = Color.BLACK
  override def tabTextSelected: Color = Color.WHITE
  override def tabTextError: Color = LightRed
  override def tabBorder: Color = MediumGray
  override def tabSeparator: Color = MediumGray
  override def tabCloseButtonBackgroundHover: Color = new Color(0, 0, 0, 64)
  override def toolbarText: Color = Color.BLACK
  override def toolbarTextSelected: Color = Color.WHITE
  override def toolbarControlBackground: Color = Color.WHITE
  override def toolbarControlBackgroundHover: Color = White2
  override def toolbarControlBackgroundPressed: Color = LightGray2
  override def toolbarControlBorder: Color = MediumGray
  override def toolbarControlBorderSelected: Color = InterfaceColors.Transparent
  override def toolbarControlFocus: Color = MediumBlue
  override def toolbarButtonHover: Color = LightGray2
  override def toolbarToolPressed: Color = MediumGray
  override def toolbarToolSelected: Color = MediumBlue
  override def toolbarImage: Color = new Color(85, 87, 112)
  override def toolbarImageSelected: Color = Color.WHITE
  override def toolbarImageDisabled: Color = new Color(100, 100, 100, 64)
  override def toolbarSeparator: Color = MediumGray
  override def infoBackground: Color = Color.WHITE
  override def infoH1Background: Color = new Color(209, 208, 255)
  override def infoH1Color: Color = new Color(19, 13, 134)
  override def infoH2Background: Color = new Color(211, 231, 255)
  override def infoH2Color: Color = new Color(0, 90, 200)
  override def infoH3Color: Color = new Color(88, 88, 88)
  override def infoH4Color: Color = new Color(115, 115, 115)
  override def infoPColor: Color = Color.BLACK
  override def infoCodeBackground: Color = LightGray
  override def infoCodeText: Color = Color.BLACK
  override def infoBlockBar: Color = new Color(96, 96, 96)
  override def infoLink: Color = new Color(0, 110, 240)
  override def checkFilled: Color = new Color(0, 173, 90)
  override def errorLabelText: Color = Color.WHITE
  override def errorLabelBackground: Color = LightRed
  override def warningLabelText: Color = Color.WHITE
  override def warningLabelBackground: Color = new Color(255, 160, 0)
  override def errorHighlight: Color = LightRed
  override def codeBackground: Color = Color.WHITE
  override def codeLineHighlight: Color = new Color(255, 255, 204)
  override def codeBracketHighlight: Color = new Color(200, 200, 255)
  override def codeSelection: Color = new Color(200, 200, 255)
  override def codeSeparator: Color = LightGray
  override def checkboxBackgroundSelected: Color = MediumBlue
  override def checkboxBackgroundSelectedHover: Color = MediumBlue2
  override def checkboxBackgroundUnselected: Color = Color.WHITE
  override def checkboxBackgroundUnselectedHover: Color = White2
  override def checkboxBackgroundDisabled: Color = MediumGray
  override def checkboxBorder: Color = MediumGray
  override def checkboxCheck: Color = Color.WHITE
  override def menuBarBorder: Color = LightGray2
  override def menuBackground: Color = Color.WHITE
  override def menuBackgroundHover: Color = MediumBlue
  override def menuBorder: Color = MediumGray
  override def menuTextHover: Color = Color.WHITE
  override def menuTextDisabled: Color = MediumGray
  override def dialogBackground: Color = White2
  override def dialogBackgroundSelected: Color = MediumBlue
  override def dialogText: Color = Color.BLACK
  override def dialogTextSelected: Color = Color.WHITE
  override def radioButtonBackground: Color = Color.WHITE
  override def radioButtonBackgroundHover: Color = White2
  override def radioButtonSelected: Color = MediumBlue
  override def radioButtonSelectedHover: Color = MediumBlue2
  override def radioButtonBorder: Color = MediumGray
  override def primaryButtonBackground: Color = MediumBlue
  override def primaryButtonBackgroundHover: Color = MediumBlue3
  override def primaryButtonBackgroundPressed: Color = MediumBlue4
  override def primaryButtonBorder: Color = MediumBlue
  override def primaryButtonText: Color = Color.WHITE
  override def secondaryButtonBackground: Color = Color.WHITE
  override def secondaryButtonBackgroundHover: Color = White2
  override def secondaryButtonBackgroundPressed: Color = LightGray2
  override def secondaryButtonBorder: Color = MediumGray
  override def secondaryButtonText: Color = Color.BLACK
  override def textAreaBackground: Color = Color.WHITE
  override def textAreaText: Color = Color.BLACK
  override def textAreaBorderEditable: Color = MediumGray
  override def textAreaBorderNoneditable: Color = LightGray
  override def tabbedPaneText: Color = Color.BLACK
  override def tabbedPaneTextSelected: Color = Color.WHITE
  override def bspaceHintBackground: Color = new Color(128, 200, 128, 64)
  override def infoIcon: Color = new Color(50, 150, 200)
  override def warningIcon: Color = new Color(220, 170, 50)
  override def errorIcon: Color = new Color(220, 50, 50)
  override def updateIcon: Color = new Color(240, 91, 0)
  override def stockBackground: Color = ClassicBeige
  override def converterBackground: Color = ClassicLightGreen
  override def announceX: Color = DarkGray
  override def announceXHovered: Color = LightGrayOutline
  override def announceXPressed: Color = MediumGray
  override def announceRelease: Color = new Color(237, 205, 255)
  override def announceAdvisory: Color = new Color(255, 194, 154)
  override def announceEvent: Color = new Color(108, 252, 221)
  override def colorPickerOutputBackground: Color = new Color(125, 125, 125)
  override def colorPickerCheckmark: Color = new Color(62, 184, 79)
  override def colorPickerCopyHover: Color = new Color(197, 197, 197)
  override def agentMonitorSeparator: Color = MediumGray

  override def colorizerTheme: ColorizerTheme = ColorizerTheme.Classic
}

object LightTheme extends ColorTheme {
  override def widgetText: Color = new Color(53, 54, 74)
  override def widgetTextError: Color = LightRed
  override def widgetHoverShadow: Color = new Color(75, 75, 75)
  override def widgetPreviewCover: Color = new Color(255, 255, 255, 100)
  override def widgetPreviewCoverNote: Color = new Color(175, 175, 175, 75)
  override def widgetHandle: Color = DarkGray
  override def displayAreaBackground: Color = Color.WHITE
  override def displayAreaText: Color = Color.BLACK
  override def textBoxBackground: Color = Color.WHITE
  override def scrollBarBackground: Color = LightGray
  override def scrollBarForeground: Color = MediumGray
  override def scrollBarForegroundHover: Color = LightGrayOutline
  override def interfaceBackground: Color = Color.WHITE
  override def commandCenterBackground: Color = LightGray
  override def commandCenterText: Color = new Color(53, 54, 74)
  override def locationToggleImage: Color = Color.BLACK
  override def commandLineBackground: Color = Color.WHITE
  override def commandOutputBackground: Color = Color.WHITE
  override def splitPaneDividerBackground: Color = MediumGray
  override def speedSliderBarBackground: Color = MediumGray
  override def speedSliderBarBackgroundFilled: Color = MediumBlue
  override def speedSliderThumb: Color = MediumBlue
  override def speedSliderThumbDisabled: Color = MediumGray
  override def buttonBackground: Color = MediumBlue
  override def buttonBackgroundHover: Color = new Color(31, 134, 255)
  override def buttonBackgroundPressed: Color = new Color(0, 52, 115)
  override def buttonBackgroundPressedHover: Color = new Color(0, 35, 77)
  override def buttonBackgroundDisabled: Color = new Color(213, 213, 213)
  override def buttonText: Color = Color.WHITE
  override def buttonTextPressed: Color = Color.WHITE
  override def buttonTextDisabled: Color = new Color(115, 115, 115)
  override def sliderBackground: Color = LightBlue
  override def sliderBarBackground: Color = MediumGray
  override def sliderBarBackgroundFilled: Color = MediumBlue
  override def sliderThumbBorder: Color = MediumBlue
  override def sliderThumbBackground: Color = Color.WHITE
  override def sliderThumbBackgroundPressed: Color = MediumBlue
  override def switchBackground: Color = LightBlue
  override def switchToggle: Color = Color.WHITE
  override def switchToggleBackgroundOn: Color = MediumBlue
  override def switchToggleBackgroundOff: Color = MediumGray
  override def chooserBackground: Color = LightBlue
  override def chooserBorder: Color = MediumBlue
  override def inputBackground: Color = LightBlue
  override def inputBorder: Color = MediumBlue
  override def viewBackground: Color = MediumGray
  override def viewBorder: Color = Color.BLACK
  override def monitorBackground: Color = LightGray1
  override def monitorBorder: Color = MediumGray
  override def plotBackground: Color = LightGray1
  override def plotBorder: Color = MediumGray
  override def plotMouseBackground: Color = LightGray
  override def plotMouseText: Color = Color.BLACK
  override def outputBackground: Color = LightGray1
  override def outputBorder: Color = MediumGray
  override def toolbarBackground: Color = LightGray
  override def tabBackground: Color = Color.WHITE
  override def tabBackgroundHover: Color = LightGray
  override def tabBackgroundSelected: Color = MediumBlue
  override def tabBackgroundError: Color = LightRed
  override def tabText: Color = Color.BLACK
  override def tabTextSelected: Color = Color.WHITE
  override def tabTextError: Color = LightRed
  override def tabBorder: Color = MediumGray
  override def tabSeparator: Color = MediumGray
  override def tabCloseButtonBackgroundHover: Color = new Color(0, 0, 0, 64)
  override def toolbarText: Color = Color.BLACK
  override def toolbarTextSelected: Color = Color.WHITE
  override def toolbarControlBackground: Color = Color.WHITE
  override def toolbarControlBackgroundHover: Color = White2
  override def toolbarControlBackgroundPressed: Color = LightGray2
  override def toolbarControlBorder: Color = MediumGray
  override def toolbarControlBorderSelected: Color = InterfaceColors.Transparent
  override def toolbarControlFocus: Color = MediumBlue
  override def toolbarButtonHover: Color = LightGray2
  override def toolbarToolPressed: Color = MediumGray
  override def toolbarToolSelected: Color = MediumBlue
  override def toolbarImage: Color = new Color(85, 87, 112)
  override def toolbarImageSelected: Color = Color.WHITE
  override def toolbarImageDisabled: Color = new Color(100, 100, 100, 64)
  override def toolbarSeparator: Color = MediumGray
  override def infoBackground: Color = Color.WHITE
  override def infoH1Background: Color = new Color(209, 208, 255)
  override def infoH1Color: Color = new Color(19, 13, 134)
  override def infoH2Background: Color = new Color(211, 231, 255)
  override def infoH2Color: Color = new Color(0, 90, 200)
  override def infoH3Color: Color = new Color(88, 88, 88)
  override def infoH4Color: Color = new Color(115, 115, 115)
  override def infoPColor: Color = Color.BLACK
  override def infoCodeBackground: Color = LightGray
  override def infoCodeText: Color = Color.BLACK
  override def infoBlockBar: Color = new Color(96, 96, 96)
  override def infoLink: Color = new Color(0, 110, 240)
  override def checkFilled: Color = new Color(0, 173, 90)
  override def errorLabelText: Color = Color.WHITE
  override def errorLabelBackground: Color = LightRed
  override def warningLabelText: Color = Color.WHITE
  override def warningLabelBackground: Color = new Color(255, 160, 0)
  override def errorHighlight: Color = LightRed
  override def codeBackground: Color = Color.WHITE
  override def codeLineHighlight: Color = new Color(255, 255, 204)
  override def codeBracketHighlight: Color = new Color(200, 200, 255)
  override def codeSelection: Color = new Color(200, 200, 255)
  override def codeSeparator: Color = LightGray
  override def checkboxBackgroundSelected: Color = MediumBlue
  override def checkboxBackgroundSelectedHover: Color = MediumBlue2
  override def checkboxBackgroundUnselected: Color = Color.WHITE
  override def checkboxBackgroundUnselectedHover: Color = White2
  override def checkboxBackgroundDisabled: Color = MediumGray
  override def checkboxBorder: Color = MediumGray
  override def checkboxCheck: Color = Color.WHITE
  override def menuBarBorder: Color = LightGray2
  override def menuBackground: Color = Color.WHITE
  override def menuBackgroundHover: Color = MediumBlue
  override def menuBorder: Color = MediumGray
  override def menuTextHover: Color = Color.WHITE
  override def menuTextDisabled: Color = MediumGray
  override def dialogBackground: Color = White2
  override def dialogBackgroundSelected: Color = MediumBlue
  override def dialogText: Color = Color.BLACK
  override def dialogTextSelected: Color = Color.WHITE
  override def radioButtonBackground: Color = Color.WHITE
  override def radioButtonBackgroundHover: Color = White2
  override def radioButtonSelected: Color = MediumBlue
  override def radioButtonSelectedHover: Color = MediumBlue2
  override def radioButtonBorder: Color = MediumGray
  override def primaryButtonBackground: Color = MediumBlue
  override def primaryButtonBackgroundHover: Color = MediumBlue3
  override def primaryButtonBackgroundPressed: Color = MediumBlue4
  override def primaryButtonBorder: Color = MediumBlue
  override def primaryButtonText: Color = Color.WHITE
  override def secondaryButtonBackground: Color = Color.WHITE
  override def secondaryButtonBackgroundHover: Color = White2
  override def secondaryButtonBackgroundPressed: Color = LightGray2
  override def secondaryButtonBorder: Color = MediumGray
  override def secondaryButtonText: Color = Color.BLACK
  override def textAreaBackground: Color = Color.WHITE
  override def textAreaText: Color = Color.BLACK
  override def textAreaBorderEditable: Color = MediumGray
  override def textAreaBorderNoneditable: Color = LightGray
  override def tabbedPaneText: Color = Color.BLACK
  override def tabbedPaneTextSelected: Color = Color.WHITE
  override def bspaceHintBackground: Color = new Color(128, 200, 128, 64)
  override def infoIcon: Color = new Color(50, 150, 200)
  override def warningIcon: Color = new Color(220, 170, 50)
  override def errorIcon: Color = new Color(220, 50, 50)
  override def updateIcon: Color = new Color(240, 91, 0)
  override def stockBackground: Color = ClassicBeige
  override def converterBackground: Color = ClassicLightGreen
  override def announceX: Color = DarkGray
  override def announceXHovered: Color = LightGrayOutline
  override def announceXPressed: Color = MediumGray
  override def announceRelease: Color = new Color(237, 205, 255)
  override def announceAdvisory: Color = new Color(255, 194, 154)
  override def announceEvent: Color = new Color(108, 202, 251)
  override def colorPickerOutputBackground: Color = new Color(125, 125, 125)
  override def colorPickerCheckmark: Color = new Color(62, 184, 79)
  override def colorPickerCopyHover: Color = new Color(197, 197, 197)
  override def agentMonitorSeparator: Color = MediumGray

  override def colorizerTheme: ColorizerTheme = ColorizerTheme.Light
}

object DarkTheme extends ColorTheme {
  override def widgetText: Color = Color.WHITE
  override def widgetTextError: Color = LightRed
  override def widgetHoverShadow: Color = new Color(75, 75, 75)
  override def widgetPreviewCover: Color = new Color(0, 0, 0, 85)
  override def widgetPreviewCoverNote: Color = new Color(100, 100, 100, 75)
  override def widgetHandle: Color = MediumGray
  override def displayAreaBackground: Color = Color.BLACK
  override def displayAreaText: Color = Color.WHITE
  override def textBoxBackground: Color = AlmostBlack
  override def scrollBarBackground: Color = new Color(40, 40, 40)
  override def scrollBarForeground: Color = DarkGray
  override def scrollBarForegroundHover: Color = LightGrayOutline
  override def interfaceBackground: Color = AlmostBlack
  override def commandCenterBackground: Color = BlueGray
  override def commandCenterText: Color = Color.WHITE
  override def locationToggleImage: Color = Color.WHITE
  override def commandLineBackground: Color = DarkGray
  override def commandOutputBackground: Color = DarkBlueGray
  override def splitPaneDividerBackground: Color = new Color(204, 204, 204)
  override def speedSliderBarBackground: Color = MediumGray
  override def speedSliderBarBackgroundFilled: Color = MediumBlue
  override def speedSliderThumb: Color = MediumBlue
  override def speedSliderThumbDisabled: Color = MediumGray
  override def buttonBackground: Color = MediumBlue
  override def buttonBackgroundHover: Color = new Color(42, 140, 255)
  override def buttonBackgroundPressed: Color = new Color(0, 58, 127)
  override def buttonBackgroundPressedHover: Color = new Color(0, 68, 149)
  override def buttonBackgroundDisabled: Color = new Color(213, 213, 213)
  override def buttonText: Color = Color.WHITE
  override def buttonTextPressed: Color = Color.WHITE
  override def buttonTextDisabled: Color = new Color(154, 154, 154)
  override def sliderBackground: Color = DarkBlue
  override def sliderBarBackground: Color = Color.BLACK
  override def sliderBarBackgroundFilled: Color = MediumBlue
  override def sliderThumbBorder: Color = MediumBlue
  override def sliderThumbBackground: Color = Color.WHITE
  override def sliderThumbBackgroundPressed: Color = MediumBlue
  override def switchBackground: Color = DarkBlue
  override def switchToggle: Color = Color.WHITE
  override def switchToggleBackgroundOn: Color = MediumBlue
  override def switchToggleBackgroundOff: Color = Color.BLACK
  override def chooserBackground: Color = DarkBlue
  override def chooserBorder: Color = MediumBlue
  override def inputBackground: Color = DarkBlue
  override def inputBorder: Color = MediumBlue
  override def viewBackground: Color = LightGrayOutline2
  override def viewBorder: Color = LightGrayOutline
  override def monitorBackground: Color = DarkGray
  override def monitorBorder: Color = LightGrayOutline
  override def plotBackground: Color = DarkGray
  override def plotBorder: Color = LightGrayOutline
  override def plotMouseBackground: Color = LightGray
  override def plotMouseText: Color = Color.BLACK
  override def outputBackground: Color = DarkGray
  override def outputBorder: Color = LightGrayOutline
  override def toolbarBackground: Color = BlueGray
  override def tabBackground: Color = DarkBlueGray
  override def tabBackgroundHover: Color = DarkBlueGray2
  override def tabBackgroundSelected: Color = MediumBlue
  override def tabBackgroundError: Color = LightRed
  override def tabText: Color = Color.WHITE
  override def tabTextSelected: Color = Color.WHITE
  override def tabTextError: Color = LightRed
  override def tabBorder: Color = LightGrayOutline
  override def tabSeparator: Color = LightGrayOutline
  override def tabCloseButtonBackgroundHover: Color = new Color(0, 0, 0, 64)
  override def toolbarText: Color = Color.WHITE
  override def toolbarTextSelected: Color = Color.WHITE
  override def toolbarControlBackground: Color = DarkBlueGray
  override def toolbarControlBackgroundHover: Color = DarkBlueGray2
  override def toolbarControlBackgroundPressed: Color = DarkBlueGray3
  override def toolbarControlBorder: Color = LightGrayOutline
  override def toolbarControlBorderSelected: Color = LightGrayOutline
  override def toolbarControlFocus: Color = Color.WHITE
  override def toolbarButtonHover: Color = MediumBlueGray
  override def toolbarToolPressed: Color = DarkBlueGray2
  override def toolbarToolSelected: Color = MediumBlue2
  override def toolbarImage: Color = new Color(168, 170, 194)
  override def toolbarImageSelected: Color = LightGray2
  override def toolbarImageDisabled: Color = new Color(150, 150, 150, 64)
  override def toolbarSeparator: Color = LightGrayOutline
  override def infoBackground: Color = AlmostBlack
  override def infoH1Background: Color = new Color(10, 0, 199)
  override def infoH1Color: Color = new Color(205, 202, 255)
  override def infoH2Background: Color = new Color(0, 80, 177)
  override def infoH2Color: Color = new Color(221, 237, 255)
  override def infoH3Color: Color = new Color(173, 183, 196)
  override def infoH4Color: Color = new Color(173, 183, 196)
  override def infoPColor: Color = Color.WHITE
  override def infoCodeBackground: Color = new Color(67, 67, 67)
  override def infoCodeText: Color = Color.WHITE
  override def infoBlockBar: Color = MediumGray
  override def infoLink: Color = new Color(0, 110, 240)
  override def checkFilled: Color = new Color(0, 173, 90)
  override def errorLabelText: Color = Color.WHITE
  override def errorLabelBackground: Color = LightRed
  override def warningLabelText: Color = Color.WHITE
  override def warningLabelBackground: Color = new Color(255, 160, 0)
  override def errorHighlight: Color = LightRed
  override def codeBackground: Color = AlmostBlack
  override def codeLineHighlight: Color = new Color(35, 35, 35)
  override def codeBracketHighlight: Color = DarkGray
  override def codeSelection: Color = DarkGray
  override def codeSeparator: Color = DarkGray
  override def checkboxBackgroundSelected: Color = MediumBlue
  override def checkboxBackgroundSelectedHover: Color = MediumBlue2
  override def checkboxBackgroundUnselected: Color = DarkBlueGray
  override def checkboxBackgroundUnselectedHover: Color = DarkBlueGray2
  override def checkboxBackgroundDisabled: Color = LightGrayOutline
  override def checkboxBorder: Color = LightGrayOutline
  override def checkboxCheck: Color = Color.WHITE
  override def menuBarBorder: Color = MediumBlueGray
  override def menuBackground: Color = BlueGray
  override def menuBackgroundHover: Color = new Color(6, 112, 237, 128)
  override def menuBorder: Color = MediumGray
  override def menuTextHover: Color = Color.WHITE
  override def menuTextDisabled: Color = MediumGray
  override def dialogBackground: Color = BlueGray
  override def dialogBackgroundSelected: Color = new Color(6, 112, 237, 128)
  override def dialogText: Color = Color.WHITE
  override def dialogTextSelected: Color = Color.WHITE
  override def radioButtonBackground: Color = DarkBlueGray
  override def radioButtonBackgroundHover: Color = DarkBlueGray2
  override def radioButtonSelected: Color = MediumBlue
  override def radioButtonSelectedHover: Color = MediumBlue2
  override def radioButtonBorder: Color = LightGrayOutline
  override def primaryButtonBackground: Color = MediumBlue
  override def primaryButtonBackgroundHover: Color = MediumBlue3
  override def primaryButtonBackgroundPressed: Color = MediumBlue4
  override def primaryButtonBorder: Color = MediumBlue
  override def primaryButtonText: Color = Color.WHITE
  override def secondaryButtonBackground: Color = DarkBlueGray
  override def secondaryButtonBackgroundHover: Color = DarkBlueGray3
  override def secondaryButtonBackgroundPressed: Color = DarkBlueGray4
  override def secondaryButtonBorder: Color = MediumGray
  override def secondaryButtonText: Color = Color.WHITE
  override def textAreaBackground: Color = DarkBlueGray
  override def textAreaText: Color = Color.WHITE
  override def textAreaBorderEditable: Color = LightGray2
  override def textAreaBorderNoneditable: Color = LightGrayOutline
  override def tabbedPaneText: Color = Color.WHITE
  override def tabbedPaneTextSelected: Color = Color.WHITE
  override def bspaceHintBackground: Color = new Color(128, 200, 128, 64)
  override def infoIcon: Color = new Color(50, 150, 200)
  override def warningIcon: Color = new Color(220, 170, 50)
  override def errorIcon: Color = new Color(220, 50, 50)
  override def updateIcon: Color = new Color(240, 91, 0)
  override def stockBackground: Color = ClassicBeige
  override def converterBackground: Color = ClassicLightGreen
  override def announceX: Color = White2
  override def announceXHovered: Color = MediumGray
  override def announceXPressed: Color = DarkGray
  override def announceRelease: Color = new Color(137, 105, 155)
  override def announceAdvisory: Color = new Color(155, 94, 54)
  override def announceEvent: Color = new Color(8, 152, 121)
  override def colorPickerOutputBackground: Color = DarkBlueGray
  override def colorPickerCheckmark: Color = new Color(62, 184, 79)
  override def colorPickerCopyHover: Color = new Color(57, 57, 57)
  override def agentMonitorSeparator: Color = LightGray2

  override def colorizerTheme: ColorizerTheme = ColorizerTheme.Dark
}

trait ThemeSync {
  def syncTheme(): Unit
}
