// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.theme

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
  private val MEDIUM_BLUE = new Color(6, 112, 237)
  private val MEDIUM_BLUE_2 = new Color(0, 102, 227)
  private val DARK_BLUE = new Color(0, 54, 117)
  private val WHITE_2 = new Color(245, 245, 245)
  private val LIGHT_GRAY = new Color(238, 238, 238)
  private val LIGHT_GRAY_2 = new Color(215, 215, 215)
  private val MEDIUM_GRAY = new Color(175, 175, 175)
  private val LIGHT_GRAY_OUTLINE = new Color(120, 120, 120)
  private val DARK_GRAY = new Color(79, 79, 79)
  private val BLUE_GRAY = new Color(70, 70, 76)
  private val MEDIUM_BLUE_GRAY = new Color(60, 60, 65)
  private val DARK_BLUE_GRAY = new Color(45, 45, 54)
  private val DARK_BLUE_GRAY_2 = new Color(35, 35, 44)
  private val LIGHT_RED = new Color(251, 96, 85)
  private val ALMOST_BLACK = new Color(22, 22, 22)

  val TRANSPARENT = new Color(0, 0, 0, 0)

  def WIDGET_TEXT =
    theme match {
      case "classic" => Color.BLACK
      case "light" => new Color(85, 87, 112)
      case "dark" => Color.WHITE
    }

  def WIDGET_TEXT_ERROR = Color.RED

  def WIDGET_HOVER_SHADOW = new Color(75, 75, 75)

  def WIDGET_PREVIEW_COVER =
    theme match {
      case "classic" | "light" => new Color(255, 255, 255, 150)
      case "dark" => new Color(0, 0, 0, 150)
    }

  def WIDGET_PREVIEW_COVER_NOTE =
    theme match {
      case "classic" | "light" => new Color(225, 225, 225, 150)
      case "dark" => new Color(30, 30, 30, 150)
    }

  def WIDGET_INTERACT_COVER =
    theme match {
      case "classic" | "light" => new Color(255, 255, 255, 100)
      case "dark" => new Color(0, 0, 0, 100)
    }

  def WIDGET_INTERACT_COVER_NOTE =
    theme match {
      case "classic" | "light" => new Color(225, 225, 225, 100)
      case "dark" => new Color(30, 30, 30, 100)
    }

  def DISPLAY_AREA_BACKGROUND =
    theme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => Color.BLACK
    }

  def DISPLAY_AREA_TEXT =
    theme match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def TEXT_BOX_BACKGROUND =
    theme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => ALMOST_BLACK
    }

  def INTERFACE_BACKGROUND =
    theme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => ALMOST_BLACK
    }

  def COMMAND_CENTER_BACKGROUND =
    theme match {
      case "classic" | "light" => LIGHT_GRAY
      case "dark" => BLUE_GRAY
    }

  def COMMAND_CENTER_TEXT = WIDGET_TEXT

  def LOCATION_TOGGLE_IMAGE =
    theme match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def COMMAND_LINE_BACKGROUND =
    theme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => DARK_GRAY
    }

  def COMMAND_OUTPUT_BACKGROUND =
    theme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => DARK_BLUE_GRAY
    }

  def SPLIT_PANE_DIVIDER_BACKGROUND =
    theme match {
      case "classic" | "light" => MEDIUM_GRAY
      case "dark" => new Color(204, 204, 204)
    }

  def BUTTON_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_LAVENDER
      case "light" | "dark" => MEDIUM_BLUE
    }

  def BUTTON_BACKGROUND_HOVER =
    theme match {
      case "classic" => CLASSIC_LAVENDER
      case "light" | "dark" => new Color(62, 150, 253)
    }

  def BUTTON_BACKGROUND_PRESSED =
    theme match {
      case "classic" => Color.BLACK
      case "light" | "dark" => new Color(0, 49, 106)
    }

  def BUTTON_BACKGROUND_PRESSED_HOVER =
    theme match {
      case "classic" => Color.BLACK
      case "light" | "dark" => new Color(9, 89, 183)
    }

  def BUTTON_BACKGROUND_DISABLED =
    theme match {
      case "classic" => CLASSIC_LAVENDER
      case "light" | "dark" => new Color(213, 213, 213)
    }

  def BUTTON_TEXT =
    theme match {
      case "classic" => Color.BLACK
      case "light" | "dark" => Color.WHITE
    }

  def BUTTON_TEXT_PRESSED =
    theme match {
      case "classic" => CLASSIC_LAVENDER
      case "light" | "dark" => Color.WHITE
    }

  def BUTTON_TEXT_DISABLED =
    theme match {
      case "classic" => Color.BLACK
      case "light" | "dark" => new Color(154, 154, 154)
    }

  def SLIDER_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_LIGHT_GREEN
      case "light" => LIGHT_BLUE
      case "dark" => DARK_BLUE
    }

  def SLIDER_BAR_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_DARK_GREEN
      case "light" => MEDIUM_GRAY
      case "dark" => Color.BLACK
    }

  def SLIDER_BAR_BACKGROUND_FILLED =
    theme match {
      case "classic" => CLASSIC_DARK_GREEN
      case "light" | "dark" => MEDIUM_BLUE
    }

  def SLIDER_THUMB_BORDER =
    theme match {
      case "classic" => CLASSIC_ORANGE
      case "light" | "dark" => MEDIUM_BLUE
    }

  def SLIDER_THUMB_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_ORANGE
      case "light" | "dark" => Color.WHITE
    }

  def SLIDER_THUMB_BACKGROUND_PRESSED =
    theme match {
      case "classic" => CLASSIC_ORANGE
      case "light" | "dark" => MEDIUM_BLUE
    }

  def SWITCH_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_LIGHT_GREEN
      case "light" => LIGHT_BLUE
      case "dark" => DARK_BLUE
    }

  def SWITCH_TOGGLE =
    theme match {
      case "classic" => CLASSIC_ORANGE
      case "light" | "dark" => Color.WHITE
    }

  def SWITCH_TOGGLE_BACKGROUND_ON =
    theme match {
      case "classic" => CLASSIC_DARK_GREEN
      case "light" | "dark" => MEDIUM_BLUE
    }

  def SWITCH_TOGGLE_BACKGROUND_OFF =
    theme match {
      case "classic" => CLASSIC_DARK_GREEN
      case "light" => MEDIUM_GRAY
      case "dark" => Color.BLACK
    }

  def CHOOSER_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_LIGHT_GREEN
      case "light" => LIGHT_BLUE
      case "dark" => DARK_BLUE
    }

  def CHOOSER_BORDER =
    theme match {
      case "classic" => CLASSIC_DARK_GREEN
      case "light" | "dark" => MEDIUM_BLUE
    }

  def INPUT_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_LIGHT_GREEN
      case "light" => LIGHT_BLUE
      case "dark" => DARK_BLUE
    }

  def INPUT_BORDER =
    theme match {
      case "classic" => CLASSIC_DARK_GREEN
      case "light" | "dark" => MEDIUM_BLUE
    }

  def GRAPHICS_BACKGROUND = Constants.ViewBackground

  def VIEW_BORDER =
    theme match {
      case "classic" | "light" => TRANSPARENT
      case "dark" => LIGHT_GRAY_OUTLINE
    }

  def MONITOR_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_BEIGE
      case "light" => LIGHT_GRAY
      case "dark" => DARK_GRAY
    }

  def MONITOR_BORDER =
    theme match {
      case "classic" | "light" => MEDIUM_GRAY
      case "dark" => LIGHT_GRAY_OUTLINE
    }

  def PLOT_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_BEIGE
      case "light" => LIGHT_GRAY
      case "dark" => DARK_GRAY
    }

  def PLOT_BORDER =
    theme match {
      case "classic" | "light" => MEDIUM_GRAY
      case "dark" => LIGHT_GRAY_OUTLINE
    }

  def PLOT_MOUSE_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_BEIGE
      case "light" | "dark" => LIGHT_GRAY
    }

  def PLOT_MOUSE_TEXT = Color.BLACK

  def OUTPUT_BACKGROUND =
    theme match {
      case "classic" => CLASSIC_BEIGE
      case "light" => LIGHT_GRAY
      case "dark" => DARK_GRAY
    }

  def OUTPUT_BORDER =
    theme match {
      case "classic" | "light" => MEDIUM_GRAY
      case "dark" => LIGHT_GRAY_OUTLINE
    }

  def TOOLBAR_BACKGROUND =
    theme match {
      case "classic" | "light" => LIGHT_GRAY
      case "dark" => BLUE_GRAY
    }

  def TAB_BACKGROUND =
    theme match {
      case "classic" | "light" => LIGHT_GRAY
      case "dark" => DARK_BLUE_GRAY
    }

  def TAB_BACKGROUND_SELECTED = MEDIUM_BLUE

  def TAB_BACKGROUND_ERROR = LIGHT_RED

  def TAB_TEXT =
    theme match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def TAB_TEXT_SELECTED = Color.WHITE

  def TAB_TEXT_ERROR = LIGHT_RED

  def TAB_BORDER =
    theme match {
      case "classic" | "light" => MEDIUM_GRAY
      case "dark" => LIGHT_GRAY_OUTLINE
    }

  def TAB_SEPARATOR =
    theme match {
      case "classic" | "light" => MEDIUM_GRAY
      case "dark" => LIGHT_GRAY_OUTLINE
    }

  def TOOLBAR_TEXT =
    theme match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def TOOLBAR_CONTROL_BACKGROUND =
    theme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => DARK_BLUE_GRAY
    }

  def TOOLBAR_CONTROL_BACKGROUND_HOVER =
    theme match {
      case "classic" | "light" => WHITE_2
      case "dark" => DARK_BLUE_GRAY_2
    }

  def TOOLBAR_CONTROL_BORDER =
    theme match {
      case "classic" | "light" => MEDIUM_GRAY
      case "dark" => LIGHT_GRAY_OUTLINE
    }

  def TOOLBAR_BUTTON_PRESSED =
    theme match {
      case "classic" | "light" => MEDIUM_GRAY
      case "dark" => DARK_BLUE_GRAY
    }

  def TOOLBAR_BUTTON_HOVER =
    theme match {
      case "classic" | "light" => LIGHT_GRAY_2
      case "dark" => MEDIUM_BLUE_GRAY
    }

  def TOOLBAR_TOOL_PRESSED =
    theme match {
      case "classic" | "light" => MEDIUM_GRAY
      case "dark" => DARK_BLUE_GRAY_2
    }

  def TOOLBAR_IMAGE =
    theme match {
      case "classic" | "light" => new Color(85, 87, 112)
      case "dark" => new Color(168, 170, 194)
    }

  def TOOLBAR_SEPARATOR =
    theme match {
      case "classic" | "light" => MEDIUM_GRAY
      case "dark" => LIGHT_GRAY_OUTLINE
    }

  def INFO_BACKGROUND =
    theme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => ALMOST_BLACK
    }

  def INFO_H1_BACKGROUND =
    theme match {
      case "classic" | "light" => new Color(209, 208, 255)
      case "dark" => new Color(10, 0, 199)
    }

  def INFO_H1_COLOR =
    theme match {
      case "classic" | "light" => new Color(19, 13, 134)
      case "dark" => new Color(205, 202, 255)
    }

  def INFO_H2_BACKGROUND =
    theme match {
      case "classic" | "light" => new Color(211, 231, 255)
      case "dark" => new Color(0, 80, 177)
    }

  def INFO_H2_COLOR =
    theme match {
      case "classic" | "light" => new Color(0, 90, 200)
      case "dark" => new Color(221, 237, 255)
    }

  def INFO_H3_COLOR =
    theme match {
      case "classic" | "light" => new Color(88, 88, 88)
      case "dark" => new Color(173, 183, 196)
    }

  def INFO_H4_COLOR =
    theme match {
      case "classic" | "light" => new Color(115, 115, 115)
      case "dark" => new Color(173, 183, 196)
    }

  def INFO_P_COLOR =
    theme match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def INFO_CODE_BACKGROUND =
    theme match {
      case "classic" | "light" => LIGHT_GRAY
      case "dark" => new Color(67, 67, 67)
    }

  def INFO_BLOCK_BAR =
    theme match {
      case "classic" | "light" => new Color(96, 96, 96)
      case "dark" => MEDIUM_GRAY
    }

  def INFO_LINK = new Color(0, 110, 240)

  def CHECK_FILLED = new Color(0, 173, 90)

  def ERROR_LABEL_BACKGROUND = LIGHT_RED

  def ERROR_HIGHLIGHT = LIGHT_RED

  def CODE_BACKGROUND =
    theme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => ALMOST_BLACK
    }

  def CODE_LINE_HIGHLIGHT =
    theme match {
      case "classic" | "light" => new Color(255, 255, 204)
      case "dark" => new Color(35, 35, 35)
    }

  def CODE_SEPARATOR =
    theme match {
      case "classic" | "light" => LIGHT_GRAY
      case "dark" => DARK_GRAY
    }

  def CHECKBOX_BACKGROUND_SELECTED = MEDIUM_BLUE

  def CHECKBOX_BACKGROUND_SELECTED_HOVER = MEDIUM_BLUE_2

  def CHECKBOX_BACKGROUND_UNSELECTED =
    theme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => DARK_BLUE_GRAY
    }

  def CHECKBOX_BACKGROUND_UNSELECTED_HOVER =
    theme match {
      case "classic"| "light" => WHITE_2
      case "dark" => DARK_BLUE_GRAY_2
    }

  def CHECKBOX_BORDER =
    theme match {
      case "classic" | "light" => MEDIUM_GRAY
      case "dark" => LIGHT_GRAY_OUTLINE
    }

  def CHECKBOX_CHECK = Color.WHITE

  def MENU_BAR_BORDER =
    theme match {
      case "classic" | "light" => LIGHT_GRAY_2
      case "dark" => MEDIUM_BLUE_GRAY
    }

  def MENU_BACKGROUND =
    theme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => BLUE_GRAY
    }

  def MENU_BACKGROUND_HOVER =
    theme match {
      case "classic" | "light" => MEDIUM_BLUE
      case "dark" => DARK_BLUE_GRAY
    }

  def MENU_BORDER = MEDIUM_GRAY

  def MENU_TEXT_HOVER =
    theme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => MEDIUM_GRAY
    }

  def MENU_TEXT_DISABLED = MEDIUM_GRAY

  def DIALOG_BACKGROUND =
    theme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => BLUE_GRAY
    }

  def DIALOG_BACKGROUND_SELECTED =
    theme match {
      case "classic" | "light" => MEDIUM_BLUE
      case "dark" => DARK_BLUE_GRAY
    }

  def DIALOG_TEXT =
    theme match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def DIALOG_TEXT_SELECTED =
    theme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => MEDIUM_GRAY
    }

  def RADIO_BUTTON_BACKGROUND =
    theme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => DARK_BLUE_GRAY
    }

  def RADIO_BUTTON_BACKGROUND_HOVER =
    theme match {
      case "classic" | "light" => WHITE_2
      case "dark" => DARK_BLUE_GRAY_2
    }

  def RADIO_BUTTON_SELECTED = MEDIUM_BLUE

  def RADIO_BUTTON_SELECTED_HOVER = MEDIUM_BLUE_2

  def RADIO_BUTTON_BORDER =
    theme match {
      case "classic" | "light" => MEDIUM_GRAY
      case "dark" => LIGHT_GRAY_OUTLINE
    }

  def TEXT_AREA_BACKGROUND =
    theme match {
      case "classic" | "light" => Color.WHITE
      case "dark" => DARK_BLUE_GRAY
    }

  def TEXT_AREA_TEXT =
    theme match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def TEXT_AREA_BORDER_EDITABLE =
    theme match {
      case "classic" | "light" => MEDIUM_GRAY
      case "dark" => LIGHT_GRAY_2
    }

  def TEXT_AREA_BORDER_NONEDITABLE =
    theme match {
      case "classic" | "light" => LIGHT_GRAY
      case "dark" => LIGHT_GRAY_OUTLINE
    }

  def TABBED_PANE_TEXT =
    theme match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }

  def TABBED_PANE_TEXT_SELECTED =
    Color.WHITE

  def BSPACE_HINT_BACKGROUND = new Color(128, 200, 128, 64)

  def INFO_ICON = new Color(50, 150, 200)

  def WARNING_ICON = new Color(220, 170, 50)

  def ERROR_ICON = new Color(220, 50, 50)

  // Syntax highlighting colors

  def COMMENT_COLOR = new Color(90, 90, 90) // gray

  def COMMAND_COLOR =
    theme match {
      case "classic" | "light" => new Color(0, 0, 170) // blue
      case "dark" => new Color(107, 107, 237) // lighter blue
    }

  def REPORTER_COLOR =
    theme match {
      case "classic" | "light" => new Color(102, 0, 150) // purple
      case "dark" => new Color(151, 71, 255) // lighter purple
    }

  def KEYWORD_COLOR =
    theme match {
      case "classic" | "light" => new Color(0, 127, 105) // bluish green
      case "dark" => new Color(6, 142, 120) // lighter bluish green
    }

  def CONSTANT_COLOR =
    theme match {
      case "classic" | "light" => new Color(237, 79, 0) // orange
      case "dark" => new Color(234, 110, 33) // lighter orange
    }

  def DEFAULT_COLOR =
    theme match {
      case "classic" | "light" => Color.BLACK
      case "dark" => Color.WHITE
    }
}

trait ThemeSync {
  def syncTheme(): Unit
}
