// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Font }

import org.nlogo.awt.Hierarchy

trait Zoomable extends Component with ZoomHelpers {
  private var baseFont: Font = getFont

  private var window: Option[ZoomableWindow] = None

  def getBaseFont: Font =
    baseFont

  def setBaseFont(font: Font): Unit = {
    baseFont = font

    zoomFont()
  }

  def zoom(): Unit = {
    zoomFont()
    zoomComponent()
  }

  override def addNotify(): Unit = {
    super.addNotify()

    Hierarchy.getWindow(this) match {
      case window: ZoomableWindow =>
        this.window = Option(window)

      case _ =>
        this.window = None
    }

    zoom()
  }

  override def getZoomFactor: Float =
    window.fold(1f)(_.getZoomFactor)

  protected def zoomComponent(): Unit = {}

  private def zoomFont(): Unit = {
    Option(baseFont).foreach { font =>
      setFont(font.deriveFont(zoom(font.getSize2D)))
    }
  }
}

class DummyZoomable extends Zoomable
