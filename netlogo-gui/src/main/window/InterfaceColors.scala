// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Color

import org.nlogo.api.Constants

object InterfaceColors {
  private var theme: String = null
  
  def setTheme(theme: String) {
    this.theme = theme
  }

  def getTheme = theme

  private val CLASSIC_LAVENDER = new Color(188, 188, 230)
  private val CLASSIC_LIGHT_GREEN = new Color(130, 188, 183)
  private val CLASSIC_DARK_GREEN = new Color(65, 94, 91)
  private val CLASSIC_ORANGE = new Color(200, 103, 103)
  private val CLASSIC_BEIGE = new Color(225, 225, 182)

  private val LIGHT_BLUE = new Color(207, 229, 255)
  private val DARK_BLUE = new Color(6, 112, 237)
  private val LIGHT_GRAY = new Color(238, 238, 238)
  private val LIGHT_RED = new Color(251, 96, 85)

  val TRANSPARENT = new Color(0, 0, 0, 0)

  def DARK_GRAY = new Color(175, 175, 175)

  def WIDGET_TEXT =
    theme match {
      case "classic" => Color.BLACK
      case "light" => new Color(85, 87, 112)
    }

  def WIDGET_HOVER_SHADOW = new Color(75, 75, 75)

  def TEXT_BOX_BACKGROUND = Color.WHITE

  def COMMAND_CENTER_BACKGROUND = LIGHT_GRAY

  def BUTTON_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_LAVENDER
      case "light" => DARK_BLUE
    }

  def BUTTON_BACKGROUND_HOVER =
    theme match {
      case "classic" => CLASSIC_LAVENDER
      case "light" => new Color(62, 150, 253)
    }

  def BUTTON_BACKGROUND_PRESSED =
    theme match {
      case "classic" => Color.BLACK
      case "light" => new Color(0, 49, 106)
    }

  def BUTTON_BACKGROUND_PRESSED_HOVER =
    theme match {
      case "classic" => Color.BLACK
      case "light" => new Color(9, 89, 183)
    }

  def BUTTON_BACKGROUND_DISABLED =
    theme match {
      case "classic" => CLASSIC_LAVENDER
      case "light" => new Color(213, 213, 213)
    }

  def BUTTON_TEXT =
    theme match {
      case "classic" => Color.BLACK
      case "light" => Color.WHITE
    }

  def BUTTON_TEXT_PRESSED =
    theme match {
      case "classic" => CLASSIC_LAVENDER
      case "light" => Color.WHITE
    }

  def BUTTON_TEXT_DISABLED =
    theme match {
      case "classic" => Color.BLACK
      case "light" => new Color(154, 154, 154)
    }

  def BUTTON_TEXT_ERROR = Color.RED

  def SLIDER_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_LIGHT_GREEN
      case "light" => LIGHT_BLUE
    }

  def SLIDER_BAR_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_DARK_GREEN
      case "light" => DARK_GRAY
    }

  def SLIDER_BAR_BACKGROUND_FILLED =
    theme match {
      case "classic" => CLASSIC_DARK_GREEN
      case "light" => DARK_BLUE
    }

  def SLIDER_THUMB_BORDER =
    theme match {
      case "classic" => CLASSIC_ORANGE
      case "light" => DARK_BLUE
    }

  def SLIDER_THUMB_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_ORANGE
      case "light" => Color.WHITE
    }

  def SLIDER_THUMB_BACKGROUND_PRESSED =
    theme match {
      case "classic" => CLASSIC_ORANGE
      case "light" => DARK_BLUE
    }

  def SWITCH_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_LIGHT_GREEN
      case "light" => LIGHT_BLUE
    }

  def SWITCH_TOGGLE =
    theme match {
      case "classic" => CLASSIC_ORANGE
      case "light" => Color.WHITE
    }
    
  def SWITCH_TOGGLE_BACKGROUND_ON =
    theme match {
      case "classic" => CLASSIC_DARK_GREEN
      case "light" => DARK_BLUE
    }

  def SWITCH_TOGGLE_BACKGROUND_OFF =
    theme match {
      case "classic" => CLASSIC_DARK_GREEN
      case "light" => DARK_GRAY
    }

  def CHOOSER_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_LIGHT_GREEN
      case "light" => LIGHT_BLUE
    }

  def CHOOSER_BORDER =
    theme match {
      case "classic" => CLASSIC_DARK_GREEN
      case "light" => DARK_BLUE
    }

  def INPUT_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_LIGHT_GREEN
      case "light" => LIGHT_BLUE
    }

  def INPUT_BORDER =
    theme match {
      case "classic" => CLASSIC_DARK_GREEN
      case "light" => DARK_BLUE
    }

  def GRAPHICS_BACKGROUND = Constants.ViewBackground

  def GRAPHICS_HANDLE = DARK_GRAY

  def MONITOR_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_BEIGE
      case "light" => LIGHT_GRAY
    }

  def MONITOR_BORDER = DARK_GRAY

  def PLOT_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_BEIGE
      case "light" => LIGHT_GRAY
    }

  def PLOT_BORDER = DARK_GRAY

  def OUTPUT_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_BEIGE
      case "light" => LIGHT_GRAY
    }

  def OUTPUT_BORDER = DARK_GRAY

  def AGENT_EDITOR_BACKGROUND = LIGHT_GRAY

  def AGENT_COMMANDER_BACKGROUND = LIGHT_GRAY

  def TOOLBAR_BACKGROUND = LIGHT_GRAY

  def TAB_BACKGROUND = LIGHT_GRAY

  def TAB_BACKGROUND_SELECTED = LIGHT_RED

  def TAB_BORDER = DARK_GRAY

  def TAB_SEPARATOR = DARK_GRAY

  def ERROR_LABEL_BACKGROUND = LIGHT_RED
}
