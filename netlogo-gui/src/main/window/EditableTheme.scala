// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Color

import org.nlogo.core.I18N
import org.nlogo.theme.ColorTheme

class EditableColor(val key: String, var value: Color) {
  def name: String =
    I18N.gui.get(s"menu.tools.themeEditor.$key")
}

trait EditableTheme(base: ColorTheme) {
  protected var name: String = base.name
  protected var isDark: Boolean = base.isDark

  protected val colorGroups: Seq[(String, Seq[EditableColor])] = Seq(
    "Menu" -> Seq(
      "menuBarBorder", "menuBackground", "menuBackgroundHover", "menuTextHover", "menuTextDisabled", "menuBorder"
    ),
    "Dialog" -> Seq(
      "dialogBackground", "dialogBackgroundSelected", "dialogText", "dialogTextSelected", "primaryButtonBackground",
      "primaryButtonBackgroundHover", "primaryButtonBackgroundPressed", "primaryButtonText", "primaryButtonBorder",
      "secondaryButtonBackground", "secondaryButtonBackgroundHover", "secondaryButtonBackgroundPressed",
      "secondaryButtonText", "secondaryButtonBorder", "infoIcon", "warningIcon", "errorIcon", "updateIcon"
    ),
    "Tabs" -> Seq(
      "tabBackground", "tabBackgroundHover", "tabBackgroundSelected", "tabBackgroundError", "tabText",
      "tabTextSelected", "tabTextError", "tabBorder", "tabSeparator", "tabCloseButtonBackgroundHover"
    ),
    "Toolbar" -> Seq(
      "toolbarBackground", "toolbarText", "toolbarTextSelected", "toolbarControlBackground",
      "toolbarControlBackgroundHover", "toolbarControlBackgroundPressed", "toolbarControlBorder",
      "toolbarControlBorderSelected", "toolbarControlFocus", "toolbarToolSelected", "toolbarImage",
      "toolbarImageSelected", "toolbarImageDisabled", "toolbarSeparator"
    ),
    "Scroll Bar" -> Seq(
      "scrollBarBackground", "scrollBarForeground", "scrollBarForegroundHover"
    ),
    "Checkbox" -> Seq(
      "checkboxBackgroundUnselected", "checkboxBackgroundUnselectedHover", "checkboxBackgroundSelected",
      "checkboxBackgroundSelectedHover", "checkboxBackgroundDisabled", "checkboxBorder", "checkboxCheck"
    ),
    "Radio Button" -> Seq(
      "radioButtonBackground", "radioButtonBackgroundHover", "radioButtonSelected", "radioButtonSelectedHover",
      "radioButtonBorder"
    ),
    "Text Area" -> Seq(
      "textAreaBackground", "textAreaText", "textAreaBorderEditable", "textAreaBorderNoneditable"
    ),
    "Command Center" -> Seq(
      "commandCenterBackground", "commandCenterText", "locationToggleImage", "commandOutputBackground"
    ),
    "Widgets" -> Seq(
      "widgetText", "widgetTextError", "widgetHoverShadow", "widgetPreviewCover", "widgetPreviewCoverNote",
      "widgetHandle", "displayAreaBackground", "displayAreaText"
    ),
    "Button Widget" -> Seq(
      "buttonBackground", "buttonBackgroundHover", "buttonBackgroundPressed", "buttonBackgroundPressedHover",
      "buttonBackgroundDisabled", "buttonText", "buttonTextPressed", "buttonTextDisabled"
    ),
    "Slider Widget" -> Seq(
      "sliderBackground", "sliderBarBackground", "sliderBarBackgroundFilled", "sliderThumbBackground",
      "sliderThumbBackgroundPressed", "sliderThumbBorder"
    ),
    "Switch Widget" -> Seq(
      "switchBackground", "switchToggle", "switchToggleBackgroundOn", "switchToggleBackgroundOff"
    ),
    "Chooser Widget" -> Seq(
      "chooserBackground", "chooserBorder"
    ),
    "Input Widget" -> Seq(
      "inputBackground", "inputBorder"
    ),
    "View Widget" -> Seq(
      "viewBackground", "viewBorder"
    ),
    "Monitor Widget" -> Seq(
      "monitorBackground", "monitorBorder"
    ),
    "Plot Widget" -> Seq(
      "plotBackground", "plotBorder", "plotMouseBackground", "plotMouseText"
    ),
    "Output Widget" -> Seq(
      "outputBackground", "outputBorder"
    ),
    "Interface Tab" -> Seq(
      "interfaceBackground", "splitPaneDividerBackground", "speedSliderBarBackground",
      "speedSliderBarBackgroundFilled", "speedSliderThumb", "speedSliderThumbDisabled"
    ),
    "Info Tab" -> Seq(
      "infoBackground", "infoH1Background", "infoH1Color", "infoH2Background", "infoH2Color", "infoH3Color",
      "infoH4Color", "infoPColor", "infoCodeBackground", "infoCodeText", "infoBlockBar", "infoLink"
    ),
    "Code Tab" -> Seq(
      "checkFilled", "errorLabelText", "errorLabelBackground", "warningLabelText", "warningLabelBackground",
      "errorHighlight", "codeBackground", "codeLineHighlight", "codeBracketHighlight", "codeSelection",
      "codeSeparator", "literalToken", "commandToken", "reporterToken", "keywordToken", "commentToken", "defaultToken"
    ),
    "System Dynamics" -> Seq(
      "stockBackground", "converterBackground"
    ),
    "Announcement Banner" -> Seq(
      "announceX", "announceXHovered", "announceXPressed", "announceRelease", "announceAdvisory", "announceEvent"
    ),
    "Color Picker" -> Seq(
      "colorPickerOutputBackground", "colorPickerCheckmark", "colorPickerCopyHover"
    ),
    "Agent Monitors" -> Seq(
      "agentMonitorSeparator"
    )
  ).map((name, values) => (name, values.map(key => new EditableColor(key, base.colors(key)))))

  protected def getStaticTheme: ColorTheme =
    ColorTheme(name, name, isDark, false, colorGroups.flatMap(_._2).map(color => (color.key, color.value)).toMap)
}
