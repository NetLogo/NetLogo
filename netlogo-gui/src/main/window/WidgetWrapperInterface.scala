// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Dimension, Point }

trait WidgetWrapperInterface {
  def getSize: Dimension
  def getPreferredSize: Dimension
  def getMaximumSize: Dimension

  def getUnselectedLocation: Point

  def setLocation(p: Point): Unit

  def validate(): Unit

  def setSize(width: Int, height: Int): Unit
  def setSize(size: Dimension): Unit

  def verticallyResizable: Boolean

  def widgetChanged(): Unit

  def gridSnap: Int

  def isNew: Boolean

  def widget(): Widget
}
