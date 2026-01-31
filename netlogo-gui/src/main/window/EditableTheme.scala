// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Color

import org.nlogo.core.{ ColorizerTheme, I18N, TokenType }
import org.nlogo.theme.ColorTheme

class EditableColor(key: String, start: Color) {
  private var color: Color = start

  def name: String =
    I18N.gui.get(s"menu.tools.themeEditor.$key")

  def get: Color =
    color

  def set(color: Color): Unit = {
    this.color = color
  }
}

trait EditableTheme(base: ColorTheme) {
  protected var name: String = base.name
  protected var isDark: Boolean = base.isDark

  protected val widgetText =
    new EditableColor("widgetText", base.widgetText)

  protected val widgetTextError =
    new EditableColor("widgetTextError", base.widgetTextError)

  protected val widgetHoverShadow =
    new EditableColor("widgetHoverShadow", base.widgetHoverShadow)

  protected val widgetPreviewCover =
    new EditableColor("widgetPreviewCover", base.widgetPreviewCover)

  protected val widgetPreviewCoverNote =
    new EditableColor("widgetPreviewCoverNote", base.widgetPreviewCoverNote)

  protected val widgetHandle =
    new EditableColor("widgetHandle", base.widgetHandle)

  protected val displayAreaBackground =
    new EditableColor("displayAreaBackground", base.displayAreaBackground)

  protected val displayAreaText =
    new EditableColor("displayAreaText", base.displayAreaText)

  protected val scrollBarBackground =
    new EditableColor("scrollBarBackground", base.scrollBarBackground)

  protected val scrollBarForeground =
    new EditableColor("scrollBarForeground", base.scrollBarForeground)

  protected val scrollBarForegroundHover =
    new EditableColor("scrollBarForegroundHover", base.scrollBarForegroundHover)

  protected val interfaceBackground =
    new EditableColor("interfaceBackground", base.interfaceBackground)

  protected val commandCenterBackground =
    new EditableColor("commandCenterBackground", base.commandCenterBackground)

  protected val commandCenterText =
    new EditableColor("commandCenterText", base.commandCenterText)

  protected val locationToggleImage =
    new EditableColor("locationToggleImage", base.locationToggleImage)

  protected val commandOutputBackground =
    new EditableColor("commandOutputBackground", base.commandOutputBackground)

  protected val splitPaneDividerBackground =
    new EditableColor("splitPaneDividerBackground", base.splitPaneDividerBackground)

  protected val speedSliderBarBackground =
    new EditableColor("speedSliderBarBackground", base.speedSliderBarBackground)

  protected val speedSliderBarBackgroundFilled =
    new EditableColor("speedSliderBarBackgroundFilled", base.speedSliderBarBackgroundFilled)

  protected val speedSliderThumb =
    new EditableColor("speedSliderThumb", base.speedSliderThumb)

  protected val speedSliderThumbDisabled =
    new EditableColor("speedSliderThumbDisabled", base.speedSliderThumbDisabled)

  protected val buttonBackground =
    new EditableColor("buttonBackground", base.buttonBackground)

  protected val buttonBackgroundHover =
    new EditableColor("buttonBackgroundHover", base.buttonBackgroundHover)

  protected val buttonBackgroundPressed =
    new EditableColor("buttonBackgroundPressed", base.buttonBackgroundPressed)

  protected val buttonBackgroundPressedHover =
    new EditableColor("buttonBackgroundPressedHover", base.buttonBackgroundPressedHover)

  protected val buttonBackgroundDisabled =
    new EditableColor("buttonBackgroundDisabled", base.buttonBackgroundDisabled)

  protected val buttonText =
    new EditableColor("buttonText", base.buttonText)

  protected val buttonTextPressed =
    new EditableColor("buttonTextPressed", base.buttonTextPressed)

  protected val buttonTextDisabled =
    new EditableColor("buttonTextDisabled", base.buttonTextDisabled)

  protected val sliderBackground =
    new EditableColor("sliderBackground", base.sliderBackground)

  protected val sliderBarBackground =
    new EditableColor("sliderBarBackground", base.sliderBarBackground)

  protected val sliderBarBackgroundFilled =
    new EditableColor("sliderBarBackgroundFilled", base.sliderBarBackgroundFilled)

  protected val sliderThumbBorder =
    new EditableColor("sliderThumbBorder", base.sliderThumbBorder)

  protected val sliderThumbBackground =
    new EditableColor("sliderThumbBackground", base.sliderThumbBackground)

  protected val sliderThumbBackgroundPressed =
    new EditableColor("sliderThumbBackgroundPressed", base.sliderThumbBackgroundPressed)

  protected val switchBackground =
    new EditableColor("switchBackground", base.switchBackground)

  protected val switchToggle =
    new EditableColor("switchToggle", base.switchToggle)

  protected val switchToggleBackgroundOn =
    new EditableColor("switchToggleBackgroundOn", base.switchToggleBackgroundOn)

  protected val switchToggleBackgroundOff =
    new EditableColor("switchToggleBackgroundOff", base.switchToggleBackgroundOff)

  protected val chooserBackground =
    new EditableColor("chooserBackground", base.chooserBackground)

  protected val chooserBorder =
    new EditableColor("chooserBorder", base.chooserBorder)

  protected val inputBackground =
    new EditableColor("inputBackground", base.inputBackground)

  protected val inputBorder =
    new EditableColor("inputBorder", base.inputBorder)

  protected val viewBackground =
    new EditableColor("viewBackground", base.viewBackground)

  protected val viewBorder =
    new EditableColor("viewBorder", base.viewBorder)

  protected val monitorBackground =
    new EditableColor("monitorBackground", base.monitorBackground)

  protected val monitorBorder =
    new EditableColor("monitorBorder", base.monitorBorder)

  protected val plotBackground =
    new EditableColor("plotBackground", base.plotBackground)

  protected val plotBorder =
    new EditableColor("plotBorder", base.plotBorder)

  protected val plotMouseBackground =
    new EditableColor("plotMouseBackground", base.plotMouseBackground)

  protected val plotMouseText =
    new EditableColor("plotMouseText", base.plotMouseText)

  protected val outputBackground =
    new EditableColor("outputBackground", base.outputBackground)

  protected val outputBorder =
    new EditableColor("outputBorder", base.outputBorder)

  protected val toolbarBackground =
    new EditableColor("toolbarBackground", base.toolbarBackground)

  protected val tabBackground =
    new EditableColor("tabBackground", base.tabBackground)

  protected val tabBackgroundHover =
    new EditableColor("tabBackgroundHover", base.tabBackgroundHover)

  protected val tabBackgroundSelected =
    new EditableColor("tabBackgroundSelected", base.tabBackgroundSelected)

  protected val tabBackgroundError =
    new EditableColor("tabBackgroundError", base.tabBackgroundError)

  protected val tabText =
    new EditableColor("tabText", base.tabText)

  protected val tabTextSelected =
    new EditableColor("tabTextSelected", base.tabTextSelected)

  protected val tabTextError =
    new EditableColor("tabTextError", base.tabTextError)

  protected val tabBorder =
    new EditableColor("tabBorder", base.tabBorder)

  protected val tabSeparator =
    new EditableColor("tabSeparator", base.tabSeparator)

  protected val tabCloseButtonBackgroundHover =
    new EditableColor("tabCloseButtonBackgroundHover", base.tabCloseButtonBackgroundHover)

  protected val toolbarText =
    new EditableColor("toolbarText", base.toolbarText)

  protected val toolbarTextSelected =
    new EditableColor("toolbarTextSelected", base.toolbarTextSelected)

  protected val toolbarControlBackground =
    new EditableColor("toolbarControlBackground", base.toolbarControlBackground)

  protected val toolbarControlBackgroundHover =
    new EditableColor("toolbarControlBackgroundHover", base.toolbarControlBackgroundHover)

  protected val toolbarControlBackgroundPressed =
    new EditableColor("toolbarControlBackgroundPressed", base.toolbarControlBackgroundPressed)

  protected val toolbarControlBorder =
    new EditableColor("toolbarControlBorder", base.toolbarControlBorder)

  protected val toolbarControlBorderSelected =
    new EditableColor("toolbarControlBorderSelected", base.toolbarControlBorderSelected)

  protected val toolbarControlFocus =
    new EditableColor("toolbarControlFocus", base.toolbarControlFocus)

  protected val toolbarToolSelected =
    new EditableColor("toolbarToolSelected", base.toolbarToolSelected)

  protected val toolbarImage =
    new EditableColor("toolbarImage", base.toolbarImage)

  protected val toolbarImageSelected =
    new EditableColor("toolbarImageSelected", base.toolbarImageSelected)

  protected val toolbarImageDisabled =
    new EditableColor("toolbarImageDisabled", base.toolbarImageDisabled)

  protected val toolbarSeparator =
    new EditableColor("toolbarSeparator", base.toolbarSeparator)

  protected val infoBackground =
    new EditableColor("infoBackground", base.infoBackground)

  protected val infoH1Background =
    new EditableColor("infoH1Background", base.infoH1Background)

  protected val infoH1Color =
    new EditableColor("infoH1Color", base.infoH1Color)

  protected val infoH2Background =
    new EditableColor("infoH2Background", base.infoH2Background)

  protected val infoH2Color =
    new EditableColor("infoH2Color", base.infoH2Color)

  protected val infoH3Color =
    new EditableColor("infoH3Color", base.infoH3Color)

  protected val infoH4Color =
    new EditableColor("infoH4Color", base.infoH4Color)

  protected val infoPColor =
    new EditableColor("infoPColor", base.infoPColor)

  protected val infoCodeBackground =
    new EditableColor("infoCodeBackground", base.infoCodeBackground)

  protected val infoCodeText =
    new EditableColor("infoCodeText", base.infoCodeText)

  protected val infoBlockBar =
    new EditableColor("infoBlockBar", base.infoBlockBar)

  protected val infoLink =
    new EditableColor("infoLink", base.infoLink)

  protected val checkFilled =
    new EditableColor("checkFilled", base.checkFilled)

  protected val errorLabelText =
    new EditableColor("errorLabelText", base.errorLabelText)

  protected val errorLabelBackground =
    new EditableColor("errorLabelBackground", base.errorLabelBackground)

  protected val warningLabelText =
    new EditableColor("warningLabelText", base.warningLabelText)

  protected val warningLabelBackground =
    new EditableColor("warningLabelBackground", base.warningLabelBackground)

  protected val errorHighlight =
    new EditableColor("errorHighlight", base.errorHighlight)

  protected val codeBackground =
    new EditableColor("codeBackground", base.codeBackground)

  protected val codeLineHighlight =
    new EditableColor("codeLineHighlight", base.codeLineHighlight)

  protected val codeBracketHighlight =
    new EditableColor("codeBracketHighlight", base.codeBracketHighlight)

  protected val codeSelection =
    new EditableColor("codeSelection", base.codeSelection)

  protected val codeSeparator =
    new EditableColor("codeSeparator", base.codeSeparator)

  protected val checkboxBackgroundSelected =
    new EditableColor("checkboxBackgroundSelected", base.checkboxBackgroundSelected)

  protected val checkboxBackgroundSelectedHover =
    new EditableColor("checkboxBackgroundSelectedHover", base.checkboxBackgroundSelectedHover)

  protected val checkboxBackgroundUnselected =
    new EditableColor("checkboxBackgroundUnselected", base.checkboxBackgroundUnselected)

  protected val checkboxBackgroundUnselectedHover =
    new EditableColor("checkboxBackgroundUnselectedHover", base.checkboxBackgroundUnselectedHover)

  protected val checkboxBackgroundDisabled =
    new EditableColor("checkboxBackgroundDisabled", base.checkboxBackgroundDisabled)

  protected val checkboxBorder =
    new EditableColor("checkboxBorder", base.checkboxBorder)

  protected val checkboxCheck =
    new EditableColor("checkboxCheck", base.checkboxCheck)

  protected val menuBarBorder =
    new EditableColor("menuBarBorder", base.menuBarBorder)

  protected val menuBackground =
    new EditableColor("menuBackground", base.menuBackground)

  protected val menuBackgroundHover =
    new EditableColor("menuBackgroundHover", base.menuBackgroundHover)

  protected val menuBorder =
    new EditableColor("menuBorder", base.menuBorder)

  protected val menuTextHover =
    new EditableColor("menuTextHover", base.menuTextHover)

  protected val menuTextDisabled =
    new EditableColor("menuTextDisabled", base.menuTextDisabled)

  protected val dialogBackground =
    new EditableColor("dialogBackground", base.dialogBackground)

  protected val dialogBackgroundSelected =
    new EditableColor("dialogBackgroundSelected", base.dialogBackgroundSelected)

  protected val dialogText =
    new EditableColor("dialogText", base.dialogText)

  protected val dialogTextSelected =
    new EditableColor("dialogTextSelected", base.dialogTextSelected)

  protected val radioButtonBackground =
    new EditableColor("radioButtonBackground", base.radioButtonBackground)

  protected val radioButtonBackgroundHover =
    new EditableColor("radioButtonBackgroundHover", base.radioButtonBackgroundHover)

  protected val radioButtonSelected =
    new EditableColor("radioButtonSelected", base.radioButtonSelected)

  protected val radioButtonSelectedHover =
    new EditableColor("radioButtonSelectedHover", base.radioButtonSelectedHover)

  protected val radioButtonBorder =
    new EditableColor("radioButtonBorder", base.radioButtonBorder)

  protected val primaryButtonBackground =
    new EditableColor("primaryButtonBackground", base.primaryButtonBackground)

  protected val primaryButtonBackgroundHover =
    new EditableColor("primaryButtonBackgroundHover", base.primaryButtonBackgroundHover)

  protected val primaryButtonBackgroundPressed =
    new EditableColor("primaryButtonBackgroundPressed", base.primaryButtonBackgroundPressed)

  protected val primaryButtonBorder =
    new EditableColor("primaryButtonBorder", base.primaryButtonBorder)

  protected val primaryButtonText =
    new EditableColor("primaryButtonText", base.primaryButtonText)

  protected val secondaryButtonBackground =
    new EditableColor("secondaryButtonBackground", base.secondaryButtonBackground)

  protected val secondaryButtonBackgroundHover =
    new EditableColor("secondaryButtonBackgroundHover", base.secondaryButtonBackgroundHover)

  protected val secondaryButtonBackgroundPressed =
    new EditableColor("secondaryButtonBackgroundPressed", base.secondaryButtonBackgroundPressed)

  protected val secondaryButtonBorder =
    new EditableColor("secondaryButtonBorder", base.secondaryButtonBorder)

  protected val secondaryButtonText =
    new EditableColor("secondaryButtonText", base.secondaryButtonText)

  protected val textAreaBackground =
    new EditableColor("textAreaBackground", base.textAreaBackground)

  protected val textAreaText =
    new EditableColor("textAreaText", base.textAreaText)

  protected val textAreaBorderEditable =
    new EditableColor("textAreaBorderEditable", base.textAreaBorderEditable)

  protected val textAreaBorderNoneditable =
    new EditableColor("textAreaBorderNoneditable", base.textAreaBorderNoneditable)

  protected val tabbedPaneText =
    new EditableColor("tabbedPaneText", base.tabbedPaneText)

  protected val tabbedPaneTextSelected =
    new EditableColor("tabbedPaneTextSelected", base.tabbedPaneTextSelected)

  protected val infoIcon =
    new EditableColor("infoIcon", base.infoIcon)

  protected val warningIcon =
    new EditableColor("warningIcon", base.warningIcon)

  protected val errorIcon =
    new EditableColor("errorIcon", base.errorIcon)

  protected val updateIcon =
    new EditableColor("updateIcon", base.updateIcon)

  protected val stockBackground =
    new EditableColor("stockBackground", base.stockBackground)

  protected val converterBackground =
    new EditableColor("converterBackground", base.converterBackground)

  protected val announceX =
    new EditableColor("announceX", base.announceX)

  protected val announceXHovered =
    new EditableColor("announceXHovered", base.announceXHovered)

  protected val announceXPressed =
    new EditableColor("announceXPressed", base.announceXPressed)

  protected val announceRelease =
    new EditableColor("announceRelease", base.announceRelease)

  protected val announceAdvisory =
    new EditableColor("announceAdvisory", base.announceAdvisory)

  protected val announceEvent =
    new EditableColor("announceEvent", base.announceEvent)

  protected val colorPickerOutputBackground =
    new EditableColor("colorPickerOutputBackground", base.colorPickerOutputBackground)

  protected val colorPickerCheckmark =
    new EditableColor("colorPickerCheckmark", base.colorPickerCheckmark)

  protected val colorPickerCopyHover =
    new EditableColor("colorPickerCopyHover", base.colorPickerCopyHover)

  protected val agentMonitorSeparator =
    new EditableColor("agentMonitorSeparator", base.agentMonitorSeparator)

  protected val literalToken =
    new EditableColor("literalToken", base.colorizerTheme.getColor(TokenType.Literal))

  protected val commandToken =
    new EditableColor("commandToken", base.colorizerTheme.getColor(TokenType.Command))

  protected val reporterToken =
    new EditableColor("reporterToken", base.colorizerTheme.getColor(TokenType.Reporter))

  protected val keywordToken =
    new EditableColor("keywordToken", base.colorizerTheme.getColor(TokenType.Keyword))

  protected val commentToken =
    new EditableColor("commentToken", base.colorizerTheme.getColor(TokenType.Comment))

  protected val defaultToken =
    new EditableColor("defaultToken", base.colorizerTheme.getColor(null))

  protected val orderedColors: Seq[EditableColor] = Seq(
    widgetText,
    widgetTextError,
    widgetHoverShadow,
    widgetPreviewCover,
    widgetPreviewCoverNote,
    widgetHandle,
    displayAreaBackground,
    displayAreaText,
    scrollBarBackground,
    scrollBarForeground,
    scrollBarForegroundHover,
    interfaceBackground,
    commandCenterBackground,
    commandCenterText,
    locationToggleImage,
    commandOutputBackground,
    splitPaneDividerBackground,
    speedSliderBarBackground,
    speedSliderBarBackgroundFilled,
    speedSliderThumb,
    speedSliderThumbDisabled,
    buttonBackground,
    buttonBackgroundHover,
    buttonBackgroundPressed,
    buttonBackgroundPressedHover,
    buttonBackgroundDisabled,
    buttonText,
    buttonTextPressed,
    buttonTextDisabled,
    sliderBackground,
    sliderBarBackground,
    sliderBarBackgroundFilled,
    sliderThumbBorder,
    sliderThumbBackground,
    sliderThumbBackgroundPressed,
    switchBackground,
    switchToggle,
    switchToggleBackgroundOn,
    switchToggleBackgroundOff,
    chooserBackground,
    chooserBorder,
    inputBackground,
    inputBorder,
    viewBackground,
    viewBorder,
    monitorBackground,
    monitorBorder,
    plotBackground,
    plotBorder,
    plotMouseBackground,
    plotMouseText,
    outputBackground,
    outputBorder,
    toolbarBackground,
    tabBackground,
    tabBackgroundHover,
    tabBackgroundSelected,
    tabBackgroundError,
    tabText,
    tabTextSelected,
    tabTextError,
    tabBorder,
    tabSeparator,
    tabCloseButtonBackgroundHover,
    toolbarText,
    toolbarTextSelected,
    toolbarControlBackground,
    toolbarControlBackgroundHover,
    toolbarControlBackgroundPressed,
    toolbarControlBorder,
    toolbarControlBorderSelected,
    toolbarControlFocus,
    toolbarToolSelected,
    toolbarImage,
    toolbarImageSelected,
    toolbarImageDisabled,
    toolbarSeparator,
    infoBackground,
    infoH1Background,
    infoH1Color,
    infoH2Background,
    infoH2Color,
    infoH3Color,
    infoH4Color,
    infoPColor,
    infoCodeBackground,
    infoCodeText,
    infoBlockBar,
    infoLink,
    checkFilled,
    errorLabelText,
    errorLabelBackground,
    warningLabelText,
    warningLabelBackground,
    errorHighlight,
    codeBackground,
    codeLineHighlight,
    codeBracketHighlight,
    codeSelection,
    codeSeparator,
    checkboxBackgroundSelected,
    checkboxBackgroundSelectedHover,
    checkboxBackgroundUnselected,
    checkboxBackgroundUnselectedHover,
    checkboxBackgroundDisabled,
    checkboxBorder,
    checkboxCheck,
    menuBarBorder,
    menuBackground,
    menuBackgroundHover,
    menuBorder,
    menuTextHover,
    menuTextDisabled,
    dialogBackground,
    dialogBackgroundSelected,
    dialogText,
    dialogTextSelected,
    radioButtonBackground,
    radioButtonBackgroundHover,
    radioButtonSelected,
    radioButtonSelectedHover,
    radioButtonBorder,
    primaryButtonBackground,
    primaryButtonBackgroundHover,
    primaryButtonBackgroundPressed,
    primaryButtonBorder,
    primaryButtonText,
    secondaryButtonBackground,
    secondaryButtonBackgroundHover,
    secondaryButtonBackgroundPressed,
    secondaryButtonBorder,
    secondaryButtonText,
    textAreaBackground,
    textAreaText,
    textAreaBorderEditable,
    textAreaBorderNoneditable,
    tabbedPaneText,
    tabbedPaneTextSelected,
    infoIcon,
    warningIcon,
    errorIcon,
    updateIcon,
    stockBackground,
    converterBackground,
    announceX,
    announceXHovered,
    announceXPressed,
    announceRelease,
    announceAdvisory,
    announceEvent,
    colorPickerOutputBackground,
    colorPickerCheckmark,
    colorPickerCopyHover,
    agentMonitorSeparator,
    literalToken,
    commandToken,
    reporterToken,
    keywordToken,
    commentToken,
    defaultToken,
  )

  protected def getStaticTheme: ColorTheme = {
    new ColorTheme(name, isDark, false) {
      override def widgetText: Color = EditableTheme.this.widgetText.get
      override def widgetTextError: Color = EditableTheme.this.widgetTextError.get
      override def widgetHoverShadow: Color = EditableTheme.this.widgetHoverShadow.get
      override def widgetPreviewCover: Color = EditableTheme.this.widgetPreviewCover.get
      override def widgetPreviewCoverNote: Color = EditableTheme.this.widgetPreviewCoverNote.get
      override def widgetHandle: Color = EditableTheme.this.widgetHandle.get
      override def displayAreaBackground: Color = EditableTheme.this.displayAreaBackground.get
      override def displayAreaText: Color = EditableTheme.this.displayAreaText.get
      override def scrollBarBackground: Color = EditableTheme.this.scrollBarBackground.get
      override def scrollBarForeground: Color = EditableTheme.this.scrollBarForeground.get
      override def scrollBarForegroundHover: Color = EditableTheme.this.scrollBarForegroundHover.get
      override def interfaceBackground: Color = EditableTheme.this.interfaceBackground.get
      override def commandCenterBackground: Color = EditableTheme.this.commandCenterBackground.get
      override def commandCenterText: Color = EditableTheme.this.commandCenterText.get
      override def locationToggleImage: Color = EditableTheme.this.locationToggleImage.get
      override def commandOutputBackground: Color = EditableTheme.this.commandOutputBackground.get
      override def splitPaneDividerBackground: Color = EditableTheme.this.splitPaneDividerBackground.get
      override def speedSliderBarBackground: Color = EditableTheme.this.speedSliderBarBackground.get
      override def speedSliderBarBackgroundFilled: Color = EditableTheme.this.speedSliderBarBackgroundFilled.get
      override def speedSliderThumb: Color = EditableTheme.this.speedSliderThumb.get
      override def speedSliderThumbDisabled: Color = EditableTheme.this.speedSliderThumbDisabled.get
      override def buttonBackground: Color = EditableTheme.this.buttonBackground.get
      override def buttonBackgroundHover: Color = EditableTheme.this.buttonBackgroundHover.get
      override def buttonBackgroundPressed: Color = EditableTheme.this.buttonBackgroundPressed.get
      override def buttonBackgroundPressedHover: Color = EditableTheme.this.buttonBackgroundPressedHover.get
      override def buttonBackgroundDisabled: Color = EditableTheme.this.buttonBackgroundDisabled.get
      override def buttonText: Color = EditableTheme.this.buttonText.get
      override def buttonTextPressed: Color = EditableTheme.this.buttonTextPressed.get
      override def buttonTextDisabled: Color = EditableTheme.this.buttonTextDisabled.get
      override def sliderBackground: Color = EditableTheme.this.sliderBackground.get
      override def sliderBarBackground: Color = EditableTheme.this.sliderBarBackground.get
      override def sliderBarBackgroundFilled: Color = EditableTheme.this.sliderBarBackgroundFilled.get
      override def sliderThumbBorder: Color = EditableTheme.this.sliderThumbBorder.get
      override def sliderThumbBackground: Color = EditableTheme.this.sliderThumbBackground.get
      override def sliderThumbBackgroundPressed: Color = EditableTheme.this.sliderThumbBackgroundPressed.get
      override def switchBackground: Color = EditableTheme.this.switchBackground.get
      override def switchToggle: Color = EditableTheme.this.switchToggle.get
      override def switchToggleBackgroundOn: Color = EditableTheme.this.switchToggleBackgroundOn.get
      override def switchToggleBackgroundOff: Color = EditableTheme.this.switchToggleBackgroundOff.get
      override def chooserBackground: Color = EditableTheme.this.chooserBackground.get
      override def chooserBorder: Color = EditableTheme.this.chooserBorder.get
      override def inputBackground: Color = EditableTheme.this.inputBackground.get
      override def inputBorder: Color = EditableTheme.this.inputBorder.get
      override def viewBackground: Color = EditableTheme.this.viewBackground.get
      override def viewBorder: Color = EditableTheme.this.viewBorder.get
      override def monitorBackground: Color = EditableTheme.this.monitorBackground.get
      override def monitorBorder: Color = EditableTheme.this.monitorBorder.get
      override def plotBackground: Color = EditableTheme.this.plotBackground.get
      override def plotBorder: Color = EditableTheme.this.plotBorder.get
      override def plotMouseBackground: Color = EditableTheme.this.plotMouseBackground.get
      override def plotMouseText: Color = EditableTheme.this.plotMouseText.get
      override def outputBackground: Color = EditableTheme.this.outputBackground.get
      override def outputBorder: Color = EditableTheme.this.outputBorder.get
      override def toolbarBackground: Color = EditableTheme.this.toolbarBackground.get
      override def tabBackground: Color = EditableTheme.this.tabBackground.get
      override def tabBackgroundHover: Color = EditableTheme.this.tabBackgroundHover.get
      override def tabBackgroundSelected: Color = EditableTheme.this.tabBackgroundSelected.get
      override def tabBackgroundError: Color = EditableTheme.this.tabBackgroundError.get
      override def tabText: Color = EditableTheme.this.tabText.get
      override def tabTextSelected: Color = EditableTheme.this.tabTextSelected.get
      override def tabTextError: Color = EditableTheme.this.tabTextError.get
      override def tabBorder: Color = EditableTheme.this.tabBorder.get
      override def tabSeparator: Color = EditableTheme.this.tabSeparator.get
      override def tabCloseButtonBackgroundHover: Color = EditableTheme.this.tabCloseButtonBackgroundHover.get
      override def toolbarText: Color = EditableTheme.this.toolbarText.get
      override def toolbarTextSelected: Color = EditableTheme.this.toolbarTextSelected.get
      override def toolbarControlBackground: Color = EditableTheme.this.toolbarControlBackground.get
      override def toolbarControlBackgroundHover: Color = EditableTheme.this.toolbarControlBackgroundHover.get
      override def toolbarControlBackgroundPressed: Color = EditableTheme.this.toolbarControlBackgroundPressed.get
      override def toolbarControlBorder: Color = EditableTheme.this.toolbarControlBorder.get
      override def toolbarControlBorderSelected: Color = EditableTheme.this.toolbarControlBorderSelected.get
      override def toolbarControlFocus: Color = EditableTheme.this.toolbarControlFocus.get
      override def toolbarToolSelected: Color = EditableTheme.this.toolbarToolSelected.get
      override def toolbarImage: Color = EditableTheme.this.toolbarImage.get
      override def toolbarImageSelected: Color = EditableTheme.this.toolbarImageSelected.get
      override def toolbarImageDisabled: Color = EditableTheme.this.toolbarImageDisabled.get
      override def toolbarSeparator: Color = EditableTheme.this.toolbarSeparator.get
      override def infoBackground: Color = EditableTheme.this.infoBackground.get
      override def infoH1Background: Color = EditableTheme.this.infoH1Background.get
      override def infoH1Color: Color = EditableTheme.this.infoH1Color.get
      override def infoH2Background: Color = EditableTheme.this.infoH2Background.get
      override def infoH2Color: Color = EditableTheme.this.infoH2Color.get
      override def infoH3Color: Color = EditableTheme.this.infoH3Color.get
      override def infoH4Color: Color = EditableTheme.this.infoH4Color.get
      override def infoPColor: Color = EditableTheme.this.infoPColor.get
      override def infoCodeBackground: Color = EditableTheme.this.infoCodeBackground.get
      override def infoCodeText: Color = EditableTheme.this.infoCodeText.get
      override def infoBlockBar: Color = EditableTheme.this.infoBlockBar.get
      override def infoLink: Color = EditableTheme.this.infoLink.get
      override def checkFilled: Color = EditableTheme.this.checkFilled.get
      override def errorLabelText: Color = EditableTheme.this.errorLabelText.get
      override def errorLabelBackground: Color = EditableTheme.this.errorLabelBackground.get
      override def warningLabelText: Color = EditableTheme.this.warningLabelText.get
      override def warningLabelBackground: Color = EditableTheme.this.warningLabelBackground.get
      override def errorHighlight: Color = EditableTheme.this.errorHighlight.get
      override def codeBackground: Color = EditableTheme.this.codeBackground.get
      override def codeLineHighlight: Color = EditableTheme.this.codeLineHighlight.get
      override def codeBracketHighlight: Color = EditableTheme.this.codeBracketHighlight.get
      override def codeSelection: Color = EditableTheme.this.codeSelection.get
      override def codeSeparator: Color = EditableTheme.this.codeSeparator.get
      override def checkboxBackgroundSelected: Color = EditableTheme.this.checkboxBackgroundSelected.get
      override def checkboxBackgroundSelectedHover: Color = EditableTheme.this.checkboxBackgroundSelectedHover.get
      override def checkboxBackgroundUnselected: Color = EditableTheme.this.checkboxBackgroundUnselected.get
      override def checkboxBackgroundUnselectedHover: Color = EditableTheme.this.checkboxBackgroundUnselectedHover.get
      override def checkboxBackgroundDisabled: Color = EditableTheme.this.checkboxBackgroundDisabled.get
      override def checkboxBorder: Color = EditableTheme.this.checkboxBorder.get
      override def checkboxCheck: Color = EditableTheme.this.checkboxCheck.get
      override def menuBarBorder: Color = EditableTheme.this.menuBarBorder.get
      override def menuBackground: Color = EditableTheme.this.menuBackground.get
      override def menuBackgroundHover: Color = EditableTheme.this.menuBackgroundHover.get
      override def menuBorder: Color = EditableTheme.this.menuBorder.get
      override def menuTextHover: Color = EditableTheme.this.menuTextHover.get
      override def menuTextDisabled: Color = EditableTheme.this.menuTextDisabled.get
      override def dialogBackground: Color = EditableTheme.this.dialogBackground.get
      override def dialogBackgroundSelected: Color = EditableTheme.this.dialogBackgroundSelected.get
      override def dialogText: Color = EditableTheme.this.dialogText.get
      override def dialogTextSelected: Color = EditableTheme.this.dialogTextSelected.get
      override def radioButtonBackground: Color = EditableTheme.this.radioButtonBackground.get
      override def radioButtonBackgroundHover: Color = EditableTheme.this.radioButtonBackgroundHover.get
      override def radioButtonSelected: Color = EditableTheme.this.radioButtonSelected.get
      override def radioButtonSelectedHover: Color = EditableTheme.this.radioButtonSelectedHover.get
      override def radioButtonBorder: Color = EditableTheme.this.radioButtonBorder.get
      override def primaryButtonBackground: Color = EditableTheme.this.primaryButtonBackground.get
      override def primaryButtonBackgroundHover: Color = EditableTheme.this.primaryButtonBackgroundHover.get
      override def primaryButtonBackgroundPressed: Color = EditableTheme.this.primaryButtonBackgroundPressed.get
      override def primaryButtonBorder: Color = EditableTheme.this.primaryButtonBorder.get
      override def primaryButtonText: Color = EditableTheme.this.primaryButtonText.get
      override def secondaryButtonBackground: Color = EditableTheme.this.secondaryButtonBackground.get
      override def secondaryButtonBackgroundHover: Color = EditableTheme.this.secondaryButtonBackgroundHover.get
      override def secondaryButtonBackgroundPressed: Color = EditableTheme.this.secondaryButtonBackgroundPressed.get
      override def secondaryButtonBorder: Color = EditableTheme.this.secondaryButtonBorder.get
      override def secondaryButtonText: Color = EditableTheme.this.secondaryButtonText.get
      override def textAreaBackground: Color = EditableTheme.this.textAreaBackground.get
      override def textAreaText: Color = EditableTheme.this.textAreaText.get
      override def textAreaBorderEditable: Color = EditableTheme.this.textAreaBorderEditable.get
      override def textAreaBorderNoneditable: Color = EditableTheme.this.textAreaBorderNoneditable.get
      override def tabbedPaneText: Color = EditableTheme.this.tabbedPaneText.get
      override def tabbedPaneTextSelected: Color = EditableTheme.this.tabbedPaneTextSelected.get
      override def infoIcon: Color = EditableTheme.this.infoIcon.get
      override def warningIcon: Color = EditableTheme.this.warningIcon.get
      override def errorIcon: Color = EditableTheme.this.errorIcon.get
      override def updateIcon: Color = EditableTheme.this.updateIcon.get
      override def stockBackground: Color = EditableTheme.this.stockBackground.get
      override def converterBackground: Color = EditableTheme.this.converterBackground.get
      override def announceX: Color = EditableTheme.this.announceX.get
      override def announceXHovered: Color = EditableTheme.this.announceXHovered.get
      override def announceXPressed: Color = EditableTheme.this.announceXPressed.get
      override def announceRelease: Color = EditableTheme.this.announceRelease.get
      override def announceAdvisory: Color = EditableTheme.this.announceAdvisory.get
      override def announceEvent: Color = EditableTheme.this.announceEvent.get
      override def colorPickerOutputBackground: Color = EditableTheme.this.colorPickerOutputBackground.get
      override def colorPickerCheckmark: Color = EditableTheme.this.colorPickerCheckmark.get
      override def colorPickerCopyHover: Color = EditableTheme.this.colorPickerCopyHover.get
      override def agentMonitorSeparator: Color = EditableTheme.this.agentMonitorSeparator.get

      override def colorizerTheme: ColorizerTheme = new ColorizerTheme {
        override def getColor(tpe: TokenType): Color = {
          tpe match {
            case TokenType.Literal => literalToken.get
            case TokenType.Command => commandToken.get
            case TokenType.Reporter => reporterToken.get
            case TokenType.Keyword => keywordToken.get
            case TokenType.Comment => commentToken.get
            case _ => defaultToken.get
          }
        }
      }
    }
  }
}
