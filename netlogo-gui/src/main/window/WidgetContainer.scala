// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Component, Rectangle }

import org.nlogo.core.{ Widget => CoreWidget }

// implemented by WidgetPanel and InterfacePanelLite - ST 10/14/03

trait WidgetContainer extends Component {
  def getUnzoomedBounds(component: Component): Rectangle

  def resetZoomInfo(widget: Widget): Unit

  def resetSizeInfo(widget: Widget): Unit

  def isZoomed: Boolean

  def loadWidget(coreWidget: CoreWidget): Widget

  def allWidgets: Seq[CoreWidget]
}
