// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Component, Rectangle }
import java.util.List
import scala.collection.Seq

// implemented by InterfacePanel and InterfacePanelLite - ST 10/14/03

trait WidgetContainer {
  def getBoundsString(widget: Widget): String
  def getUnzoomedBounds(component: Component): Rectangle
  def resetZoomInfo(widget: Widget): Unit
  def resetSizeInfo(widget: Widget): Unit
  def isZoomed: Boolean
  def loadWidget(strings: Seq[String], modelVersion: String): Widget
  def getWidgetsForSaving: List[Widget] // Added by NP 2012-09-13 so ReviewTab can access it
}
