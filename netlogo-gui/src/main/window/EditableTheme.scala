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
  private implicit val i18nPrefix: I18N.Prefix = I18N.Prefix("menu.tools.themeEditor")

  protected var name: String = base.name
  protected var isDark: Boolean = base.isDark

  protected val colorGroups: Seq[(String, Seq[EditableColor])] = Seq(
    I18N.gui("menuGroup") -> Seq(
      "menuBarBorder", "menuBackground", "menuBackgroundHover", "menuTextHover", "menuTextDisabled", "menuBorder"
    ),
    I18N.gui("dialogGroup") -> Seq(
      "dialogBackground", "dialogBackgroundSelected", "dialogText", "dialogTextSelected", "primaryButtonBackground",
      "primaryButtonBackgroundHover", "primaryButtonBackgroundPressed", "primaryButtonText", "primaryButtonBorder",
      "secondaryButtonBackground", "secondaryButtonBackgroundHover", "secondaryButtonBackgroundPressed",
      "secondaryButtonText", "secondaryButtonBorder", "infoIcon", "warningIcon", "errorIcon", "updateIcon"
    ),
    I18N.gui("tabsGroup") -> Seq(
      "tabBackground", "tabBackgroundHover", "tabBackgroundSelected", "tabBackgroundError", "tabText",
      "tabTextSelected", "tabTextError", "tabBorder", "tabSeparator", "tabCloseButtonBackgroundHover"
    ),
    I18N.gui("toolbarGroup") -> Seq(
      "toolbarBackground", "toolbarText", "toolbarTextSelected", "toolbarControlBackground",
      "toolbarControlBackgroundHover", "toolbarControlBackgroundPressed", "toolbarControlBorder",
      "toolbarControlBorderSelected", "toolbarControlFocus", "toolbarToolSelected", "toolbarImage",
      "toolbarImageSelected", "toolbarImageDisabled", "toolbarSeparator"
    ),
    I18N.gui("scrollBarGroup") -> Seq(
      "scrollBarBackground", "scrollBarForeground", "scrollBarForegroundHover"
    ),
    I18N.gui("checkboxGroup") -> Seq(
      "checkboxBackgroundUnselected", "checkboxBackgroundUnselectedHover", "checkboxBackgroundSelected",
      "checkboxBackgroundSelectedHover", "checkboxBackgroundDisabled", "checkboxBorder", "checkboxCheck"
    ),
    I18N.gui("radioButtonGroup") -> Seq(
      "radioButtonBackground", "radioButtonBackgroundHover", "radioButtonSelected", "radioButtonSelectedHover",
      "radioButtonBorder"
    ),
    I18N.gui("textAreaGroup") -> Seq(
      "textAreaBackground", "textAreaText", "textAreaBorderEditable", "textAreaBorderNoneditable"
    ),
    I18N.gui("commandCenterGroup") -> Seq(
      "commandCenterBackground", "commandCenterText", "locationToggleImage", "commandOutputBackground"
    ),
    I18N.gui("widgetsGroup") -> Seq(
      "widgetText", "widgetTextError", "widgetHoverShadow", "widgetPreviewCover", "widgetPreviewCoverNote",
      "widgetHandle", "displayAreaBackground", "displayAreaText"
    ),
    I18N.gui("buttonWidgetGroup") -> Seq(
      "buttonBackground", "buttonBackgroundHover", "buttonBackgroundPressed", "buttonBackgroundPressedHover",
      "buttonBackgroundDisabled", "buttonText", "buttonTextPressed", "buttonTextDisabled"
    ),
    I18N.gui("sliderWidgetGroup") -> Seq(
      "sliderBackground", "sliderBarBackground", "sliderBarBackgroundFilled", "sliderThumbBackground",
      "sliderThumbBackgroundPressed", "sliderThumbBorder"
    ),
    I18N.gui("switchWidgetGroup") -> Seq(
      "switchBackground", "switchToggle", "switchToggleBackgroundOn", "switchToggleBackgroundOff"
    ),
    I18N.gui("chooserWidgetGroup") -> Seq(
      "chooserBackground", "chooserBorder"
    ),
    I18N.gui("inputWidgetGroup") -> Seq(
      "inputBackground", "inputBorder"
    ),
    I18N.gui("viewWidgetGroup") -> Seq(
      "viewBackground", "viewBorder"
    ),
    I18N.gui("monitorWidgetGroup") -> Seq(
      "monitorBackground", "monitorBorder"
    ),
    I18N.gui("plotWidgetGroup") -> Seq(
      "plotBackground", "plotBorder", "plotMouseBackground", "plotMouseText"
    ),
    I18N.gui("outputWidgetGroup") -> Seq(
      "outputBackground", "outputBorder"
    ),
    I18N.gui("interfaceTabGroup") -> Seq(
      "interfaceBackground", "splitPaneDividerBackground", "speedSliderBarBackground",
      "speedSliderBarBackgroundFilled", "speedSliderThumb", "speedSliderThumbDisabled"
    ),
    I18N.gui("infoTabGroup") -> Seq(
      "infoBackground", "infoH1Background", "infoH1Color", "infoH2Background", "infoH2Color", "infoH3Color",
      "infoH4Color", "infoPColor", "infoCodeBackground", "infoCodeText", "infoBlockBar", "infoLink"
    ),
    I18N.gui("codeTabGroup") -> Seq(
      "checkFilled", "errorLabelText", "errorLabelBackground", "warningLabelText", "warningLabelBackground",
      "errorHighlight", "codeBackground", "codeLineHighlight", "codeBracketHighlight", "codeSelection",
      "codeSeparator", "literalToken", "commandToken", "reporterToken", "keywordToken", "commentToken", "defaultToken"
    ),
    I18N.gui("systemDynamicsGroup") -> Seq(
      "stockBackground", "converterBackground"
    ),
    I18N.gui("announcementBannerGroup") -> Seq(
      "announceX", "announceXHovered", "announceXPressed", "announceRelease", "announceAdvisory", "announceEvent"
    ),
    I18N.gui("colorPickerGroup") -> Seq(
      "colorPickerOutputBackground", "colorPickerCheckmark", "colorPickerCopyHover"
    ),
    I18N.gui("agentMonitorGroup") -> Seq(
      "agentMonitorSeparator"
    )
  ).map((name, values) => (name, values.map(key => new EditableColor(key, base.colors(key)))))

  protected def getStaticTheme: ColorTheme =
    ColorTheme(name, name, isDark, false, colorGroups.flatMap(_._2).map(color => (color.key, color.value)).toMap)
}
