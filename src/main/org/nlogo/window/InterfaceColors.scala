// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Color
import org.nlogo.api.Constants

final object InterfaceColors {
  // now that we've toned down the graphics window colors, I think
  // maybe we need to tone down the colors in the rest of the Interface
  // tab a bit, to keep the same feeling of distinctness of the 2D View
  // from the rest of the Interface tab - ST 4/28/05
  private val SATURATION_ADJUSTMENT = 0.85

  // color graveyard
  // private val LIGHT_GREEN = new Color(180, 230, 180)
  // private val DUSTY_ADOBE_ROSE = new Color(221, 162, 153)
  // private val LIGHT_ORANGE = new Color(235, 194 /*183*/, 139)
  // private val LIGHT_GRAY = new Color(230, 230, 230 )
  // private val ICE = new Color(200, 200, 230 )

  private val LIGHT_BLUE      = hsb(0.667, 0.217, 0.902)
  private val LIGHT_TURQUOISE = hsb(0.485, 0.362, 0.737)
  private val STRAW           = hsb(0.167, 0.222, 0.882)
  private val MEDIUM_RED      = hsb(0.0, 0.57, 0.784)
  private val MEDIUM_BLUE     = hsb(0.667, 0.565, 0.902)

  val TEXT_BOX_BACKGROUND = Color.WHITE
  val TRANSPARENT = new Color(0, 0, 0, 0)
  val COMMAND_CENTER_BACKGROUND = LIGHT_BLUE
  val BUTTON_BACKGROUND = LIGHT_BLUE
  val SLIDER_BACKGROUND = LIGHT_TURQUOISE
  val SLIDER_HANDLE = MEDIUM_RED
  val SWITCH_BACKGROUND = LIGHT_TURQUOISE
  val SWITCH_HANDLE = MEDIUM_RED
  val GRAPHICS_BACKGROUND = Constants.ViewBackground
  val GRAPHICS_HANDLE = MEDIUM_BLUE
  val MONITOR_BACKGROUND = STRAW
  val PLOT_BACKGROUND = STRAW
  val AGENT_EDITOR_BACKGROUND = LIGHT_TURQUOISE
  val AGENT_COMMANDER_BACKGROUND = LIGHT_BLUE

  private def hsb(h: Double, s: Double, b: Double) =
    Color.getHSBColor(h.toFloat, (SATURATION_ADJUSTMENT * s).toFloat, b.toFloat)
}
