// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

case class PlotPenState(
  x: Double = 0.0,
  color: Int = java.awt.Color.BLACK.getRGB,
  interval: Double = 1.0,
  mode: Int = PlotPenInterface.LineMode,
  isDown: Boolean = true,
  hidden: Boolean = false
)
