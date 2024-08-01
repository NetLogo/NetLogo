// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import java.awt.Color;

import org.nlogo.api.Constants;

public final class InterfaceColors {

  // this class is not instantiable
  private InterfaceColors() {
    throw new IllegalStateException();
  }

  // now that we've toned down the graphics window colors, I think
  // maybe we need to tone down the colors in the rest of the Interface
  // tab a bit, to keep the same feeling of distinctness of the 2D View
  // from the rest of the Interface tab - ST 4/28/05
  private static final double SATURATION_ADJUSTMENT = 0.85;

  // color graveyard
  // private static final Color LIGHT_GREEN = new Color( 180 , 230 , 180 ) ;
  // private static final Color DUSTY_ADOBE_ROSE = new Color( 221 , 162, 153 ) ;
  // private static final Color LIGHT_ORANGE = new Color( 235 , 194 /*183*/ , 139 ) ;
  // private static final Color LIGHT_GRAY = new Color( 230 , 230 , 230 ) ;
  // private static final Color ICE = new Color( 200 , 200 , 230 ) ;
  // private static final Color LIGHT_BLUE = hsb(0.667, 0.217, 0.902);
  // private static final Color LIGHT_TURQUOISE = hsb(0.485, 0.362, 0.737);
  // private static final Color STRAW = hsb(0.167, 0.222, 0.882);
  // private static final Color MEDIUM_RED = hsb(0.0, 0.57, 0.784);
  // private static final Color MEDIUM_BLUE = hsb(0.667, 0.565, 0.902);


  // new flatlaf colors
  public static final Color LIGHT_GRAY = new Color(240, 240, 240);
  public static final Color DARK_GRAY = new Color(171, 178, 186);
  public static final Color INPUT_BACKGROUND = new Color(207, 229, 255);
  public static final Color OUTPUT_BACKGROUND = new Color(231, 231, 237);
  public static final Color MEDIUM_BLUE = new Color(6, 112, 237);

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

  private static Color hsb(double h, double s, double b) {
    return Color.getHSBColor
        ((float) h,
            (float) (SATURATION_ADJUSTMENT * s),
            (float) b);
  }

}
