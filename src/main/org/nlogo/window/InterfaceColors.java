// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.api.Constants;

public final strictfp class InterfaceColors {

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
  // private static final java.awt.Color LIGHT_GREEN = new java.awt.Color( 180 , 230 , 180 ) ;
  // private static final java.awt.Color DUSTY_ADOBE_ROSE = new java.awt.Color( 221 , 162, 153 ) ;
  // private static final java.awt.Color LIGHT_ORANGE = new java.awt.Color( 235 , 194 /*183*/ , 139 ) ;
  // private static final java.awt.Color LIGHT_GRAY = new java.awt.Color( 230 , 230 , 230 ) ;
  // private static final java.awt.Color ICE = new java.awt.Color( 200 , 200 , 230 ) ;

  private static final java.awt.Color LIGHT_BLUE = hsb(0.667, 0.217, 0.902);
  private static final java.awt.Color LIGHT_TURQUOISE = hsb(0.485, 0.362, 0.737);
  private static final java.awt.Color STRAW = hsb(0.167, 0.222, 0.882);
  private static final java.awt.Color MEDIUM_RED = hsb(0.0, 0.57, 0.784);
  private static final java.awt.Color MEDIUM_BLUE = hsb(0.667, 0.565, 0.902);

  public static final java.awt.Color TEXT_BOX_BACKGROUND = java.awt.Color.WHITE;
  public static final java.awt.Color TRANSPARENT = new java.awt.Color(0, 0, 0, 0);
  public static final java.awt.Color COMMAND_CENTER_BACKGROUND = LIGHT_BLUE;
  public static final java.awt.Color BUTTON_BACKGROUND = LIGHT_BLUE;
  public static final java.awt.Color SLIDER_BACKGROUND = LIGHT_TURQUOISE;
  public static final java.awt.Color SLIDER_HANDLE = MEDIUM_RED;
  public static final java.awt.Color SWITCH_BACKGROUND = LIGHT_TURQUOISE;
  public static final java.awt.Color SWITCH_HANDLE = MEDIUM_RED;
  public static final java.awt.Color GRAPHICS_BACKGROUND = Constants.ViewBackground();
  public static final java.awt.Color GRAPHICS_HANDLE = MEDIUM_BLUE;
  public static final java.awt.Color MONITOR_BACKGROUND = STRAW;
  public static final java.awt.Color PLOT_BACKGROUND = STRAW;
  public static final java.awt.Color AGENT_EDITOR_BACKGROUND = LIGHT_TURQUOISE;
  public static final java.awt.Color AGENT_COMMANDER_BACKGROUND = LIGHT_BLUE;

  private static java.awt.Color hsb(double h, double s, double b) {
    return java.awt.Color.getHSBColor
        ((float) h,
            (float) (SATURATION_ADJUSTMENT * s),
            (float) b);
  }

}
