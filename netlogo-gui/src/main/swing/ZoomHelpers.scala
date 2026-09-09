// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Container, Dimension, Insets, Rectangle }

trait ZoomHelpers {
  def getZoomFactor: Float

  def zoom(value: Int): Int =
    (value * getZoomFactor).toInt

  def zoom(value: Float): Float =
    value * getZoomFactor

  def zoomClamped(value: Int): Int =
    (value * getZoomFactor).toInt.max(1)

  def zoomClamped(value: Float): Float =
    (value * getZoomFactor).max(1f)

  def zoomSize(size: Dimension): Dimension =
    new Dimension(zoom(size.width), zoom(size.height))

  def zoomInsets(insets: Insets): Insets =
    new Insets(zoom(insets.top), zoom(insets.left), zoom(insets.bottom), zoom(insets.right))

  def zoomBounds(bounds: Rectangle): Rectangle =
    new Rectangle(zoom(bounds.x), zoom(bounds.y), zoom(bounds.width), zoom(bounds.height))

  def zoomMenuBar(menuBar: MenuBar): Unit = {
    menuBar.getComponents.foreach {
      case zoomable: Zoomable =>
        zoomable.zoom()

      case _ =>
    }
  }

  def unzoomBounds(bounds: Rectangle): Rectangle =
    new Rectangle(unzoom(bounds.x), unzoom(bounds.y), unzoom(bounds.width), unzoom(bounds.height))

  protected def zoomComponents(component: Component): Unit = {
    component match {
      case container: Container =>
        container.getComponents.foreach(zoomComponents)

      case _ =>
    }

    component match {
      case zoomable: Zoomable =>
        zoomable.zoom()

      case _ =>
    }
  }

  private def unzoom(value: Int): Int =
    (value / getZoomFactor).toInt
}
