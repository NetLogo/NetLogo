// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ Color, Dimension, Graphics, Graphics2D, Image, RenderingHints }
import javax.swing.JPanel

import org.nlogo.awt.Images.loadImageFile
import org.nlogo.window.GraphicsPreviewInterface

class GraphicsPreview
  extends JPanel
  // not JComponent otherwise super.paintComponent() doesn't paint the
  // background color for reasons I can't fathom - ST 8/3/03
  with GraphicsPreviewInterface {

  private var image: Option[Image] = None

  setBackground(Color.BLACK)
  setOpaque(true)
  setPreferredSize(new Dimension(400, 400))
  setMinimumSize(getPreferredSize)
  setMaximumSize(getPreferredSize)

  def setImage(imagePath: String): Unit =
    setImage(Option(imagePath).map(loadImageFile(_, false)).orNull)

  def setImage(newImage: Image): Unit = {
    image = Option(newImage)
    repaint()
  }

  override def paintComponent(g: Graphics): Unit = {
    image match {
      case None => super.paintComponent(g)
      case Some(image) =>
        g.asInstanceOf[Graphics2D].setRenderingHint(
          RenderingHints.KEY_RENDERING,
          RenderingHints.VALUE_RENDER_QUALITY)
        val ratio = image.getWidth(null) / image.getHeight(null).toDouble
        val (w, h) = ratio match {
          case 1.0          => (400, 400)
          case r if r < 1.0 => ((400 * r).toInt, 400)
          case r if r > 1.0 => (400, (400 * (1 / r)).toInt)
          case _            => throw new Exception(s"Unexpected ratio: $ratio")
        }
        val x = (400 - w) / 2
        val y = (400 - h) / 2
        if (ratio != 1.0) {
          g.setColor(Color.black)
          g.fillRect(0, 0, 400, 400)
        }
        g.drawImage(image, x, y, w, h, this)
    }
  }
}
