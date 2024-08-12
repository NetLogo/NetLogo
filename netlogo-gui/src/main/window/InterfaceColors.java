// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import java.awt.Color;

import org.nlogo.api.Constants;

public final class InterfaceColors {
  public static final Color LIGHT_GRAY = new Color(238, 238, 238);
  public static final Color DARK_GRAY = new Color(175, 175, 175);
  public static final Color INPUT_BACKGROUND = new Color(207, 229, 255);
  public static final Color OUTPUT_BACKGROUND = new Color(231, 231, 237);
  public static final Color MEDIUM_BLUE = new Color(6, 112, 237);

  public static final Color WIDGET_HOVER_SHADOW = new Color(75, 75, 75);
  public static final Color TEXT_BOX_BACKGROUND = Color.WHITE;
  public static final Color TRANSPARENT = new Color(0, 0, 0, 0);
  public static final Color WIDGET_TEXT = new Color(85, 87, 112);
  public static final Color COMMAND_CENTER_BACKGROUND = LIGHT_GRAY;
  public static final Color BUTTON_BACKGROUND = MEDIUM_BLUE;
  public static final Color BUTTON_BACKGROUND_HOVER = new Color(62, 150, 253);
  public static final Color BUTTON_BACKGROUND_PRESSED = new Color(0, 49, 106);
  public static final Color BUTTON_BACKGROUND_PRESSED_HOVER = new Color(9, 89, 183);
  public static final Color BUTTON_BACKGROUND_DISABLED = new Color(213, 213, 213);
  public static final Color BUTTON_TEXT = Color.WHITE;
  public static final Color BUTTON_TEXT_DISABLED = new Color(154, 154, 154);
  public static final Color BUTTON_TEXT_ERROR = Color.RED;
  public static final Color SLIDER_BACKGROUND = INPUT_BACKGROUND;
  public static final Color SLIDER_BAR_BACKGROUND = DARK_GRAY;
  public static final Color SLIDER_BAR_BACKGROUND_FILLED = MEDIUM_BLUE;
  public static final Color SLIDER_THUMB_BORDER = MEDIUM_BLUE;
  public static final Color SLIDER_THUMB_BACKGROUND = Color.WHITE;
  public static final Color SLIDER_THUMB_BACKGROUND_PRESSED = MEDIUM_BLUE;
  public static final Color SWITCH_BACKGROUND = INPUT_BACKGROUND;
  public static final Color SWITCH_TOGGLE = Color.WHITE;
  public static final Color SWITCH_TOGGLE_BACKGROUND_ON = MEDIUM_BLUE;
  public static final Color SWITCH_TOGGLE_BACKGROUND_OFF = DARK_GRAY;
  public static final Color CHOOSER_BACKGROUND = INPUT_BACKGROUND;
  public static final Color CHOOSER_BORDER = MEDIUM_BLUE;
  public static final Color INPUT_BORDER = MEDIUM_BLUE;
  public static final Color GRAPHICS_BACKGROUND = Constants.ViewBackground();
  public static final Color GRAPHICS_HANDLE = DARK_GRAY;
  public static final Color MONITOR_BACKGROUND = OUTPUT_BACKGROUND;
  public static final Color MONITOR_BORDER = DARK_GRAY;
  public static final Color PLOT_BACKGROUND = OUTPUT_BACKGROUND;
  public static final Color PLOT_BORDER = DARK_GRAY;
  public static final Color OUTPUT_BORDER = DARK_GRAY;

  public static final Color AGENT_EDITOR_BACKGROUND = LIGHT_GRAY;
  public static final Color AGENT_COMMANDER_BACKGROUND = LIGHT_GRAY;
  public static final Color TOOLBAR_BACKGROUND = LIGHT_GRAY;
  public static final Color TAB_BACKGROUND = LIGHT_GRAY;
  public static final Color TAB_BACKGROUND_SELECTED = new Color(251, 96, 85);
  public static final Color TAB_BORDER = DARK_GRAY;
  public static final Color TAB_SEPARATOR = DARK_GRAY;
}
