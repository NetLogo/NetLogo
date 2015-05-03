// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Dimension, Font, Graphics }

abstract class SliderPainter {
  def getMinimumSize: Dimension
  def getPreferredSize(font: Font): Dimension
  def getMaximumSize: Dimension
  def setToolTipText(text: String): Unit
  def doLayout(): Unit
  def paintComponent(g: Graphics): Unit
  def dettach(): Unit
}
