// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Font }

trait Zoomable extends Component {
  private var baseFont: Font = getFont

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

    zoom()
  }

  protected def zoomComponent(): Unit = {}

  private def zoomFont(): Unit = {
    Option(baseFont).foreach { font =>
      setFont(font.deriveFont(Utils.zoom(font.getSize2D)))
    }
  }
}
