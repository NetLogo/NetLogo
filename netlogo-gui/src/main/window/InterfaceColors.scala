// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import java.awt.Color;
import java.util.prefs.Preferences

import org.nlogo.api.Constants;

object InterfaceColors {
  class ColorTheme
  case object ClassicTheme extends ColorTheme
  case object LightTheme extends ColorTheme

  private val prefs = Preferences.userRoot.node("/org/nlogo/NetLogo")

  private var theme: ColorTheme =
    prefs.get("colorTheme", "light") match {
      case "classic" => ClassicTheme
      case "light" => LightTheme
    }
  
  def setTheme(theme: ColorTheme) {
    this.theme = theme

    prefs.put("colorTheme", theme match {
      case ClassicTheme => "classic"
      case LightTheme => "light"
    })
  }

  def getTheme: ColorTheme = theme

  def LIGHT_GRAY = new Color(238, 238, 238)
  def DARK_GRAY = new Color(175, 175, 175)
  def INPUT_BACKGROUND = new Color(207, 229, 255)
  def OUTPUT_BACKGROUND = new Color(231, 231, 237)
  def MEDIUM_BLUE = new Color(6, 112, 237)

  def WIDGET_HOVER_SHADOW = new Color(75, 75, 75)
  def TEXT_BOX_BACKGROUND = Color.WHITE
  def TRANSPARENT = new Color(0, 0, 0, 0)
  def WIDGET_TEXT = new Color(85, 87, 112)
  def COMMAND_CENTER_BACKGROUND = LIGHT_GRAY
  def BUTTON_BACKGROUND = MEDIUM_BLUE
  def BUTTON_BACKGROUND_HOVER = new Color(62, 150, 253)
  def BUTTON_BACKGROUND_PRESSED = new Color(0, 49, 106)
  def BUTTON_BACKGROUND_PRESSED_HOVER = new Color(9, 89, 183)
  def BUTTON_BACKGROUND_DISABLED = new Color(213, 213, 213)
  def BUTTON_TEXT = Color.WHITE
  def BUTTON_TEXT_DISABLED = new Color(154, 154, 154)
  def BUTTON_TEXT_ERROR = Color.RED
  def SLIDER_BACKGROUND = INPUT_BACKGROUND
  def SLIDER_BAR_BACKGROUND = DARK_GRAY
  def SLIDER_BAR_BACKGROUND_FILLED = MEDIUM_BLUE
  def SLIDER_THUMB_BORDER = MEDIUM_BLUE
  def SLIDER_THUMB_BACKGROUND = Color.WHITE
  def SLIDER_THUMB_BACKGROUND_PRESSED = MEDIUM_BLUE
  def SWITCH_BACKGROUND = INPUT_BACKGROUND
  def SWITCH_TOGGLE = Color.WHITE
  def SWITCH_TOGGLE_BACKGROUND_ON = MEDIUM_BLUE
  def SWITCH_TOGGLE_BACKGROUND_OFF = DARK_GRAY
  def CHOOSER_BACKGROUND = INPUT_BACKGROUND
  def CHOOSER_BORDER = MEDIUM_BLUE
  def INPUT_BORDER = MEDIUM_BLUE
  def GRAPHICS_BACKGROUND = Constants.ViewBackground
  def GRAPHICS_HANDLE = DARK_GRAY
  def MONITOR_BACKGROUND = OUTPUT_BACKGROUND
  def MONITOR_BORDER = DARK_GRAY
  def PLOT_BACKGROUND = OUTPUT_BACKGROUND
  def PLOT_BORDER = DARK_GRAY
  def OUTPUT_BORDER = DARK_GRAY

  def AGENT_EDITOR_BACKGROUND = LIGHT_GRAY
  def AGENT_COMMANDER_BACKGROUND = LIGHT_GRAY
  def TOOLBAR_BACKGROUND = LIGHT_GRAY
  def TAB_BACKGROUND = LIGHT_GRAY
  def TAB_BACKGROUND_SELECTED = new Color(251, 96, 85)
  def TAB_BORDER = DARK_GRAY
  def TAB_SEPARATOR = DARK_GRAY
}
