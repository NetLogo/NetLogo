// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Font }

trait Zoomable extends Component with ZoomHelpers {
  private var baseFont: Font = getFont

  private var zoomRoot: Option[ZoomHelpers] = None

  def getZoomRoot: Option[ZoomHelpers] =
    zoomRoot

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

    zoomRoot = findZoomRoot(getParent)

    zoom()
  }

  override def getZoomFactor: Float =
    zoomRoot.fold(1f)(_.getZoomFactor)

  protected def zoomComponent(): Unit = {}

  private def zoomFont(): Unit = {
    Option(baseFont).foreach { font =>
      setFont(font.deriveFont(zoom(font.getSize2D)))
    }
  }

  private def findZoomRoot(component: Component): Option[ZoomHelpers] = {
    Option(component).collect {
      case window: ZoomableWindow =>
        window
    }.orElse(findZoomRoot(component.getParent)).orElse {
      component match {
        case zoom: ZoomHelpers =>
          Option(zoom)

        case _ =>
          None
      }
    }
  }
}

class DummyZoomable extends Zoomable
