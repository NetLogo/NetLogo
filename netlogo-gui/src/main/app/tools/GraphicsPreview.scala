// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ Color, Dimension, Graphics, Graphics2D, Image, RenderingHints }
import javax.swing.JPanel

import org.nlogo.awt.Images.loadImageFile
import org.nlogo.swing.{ PreferredSize, Utils }
import org.nlogo.window.GraphicsPreviewInterface

// not JComponent otherwise super.paintComponent() doesn't paint the
// background color for reasons I can't fathom - ST 8/3/03
class GraphicsPreview extends JPanel with GraphicsPreviewInterface with PreferredSize {
  private var image: Option[Image] = None

  setBackground(Color.BLACK)
  setOpaque(true)

  def setImage(imagePath: String): Unit =
    setImage(Option(imagePath).map(loadImageFile(_, false)).orNull)

  def setImage(newImage: Image): Unit = {
    if (!image.exists(_ == newImage)) {
      image = Option(newImage)
      repaint()
    }
  }

  override def getPreferredSize: Dimension =
    new Dimension(Utils.zoom(400), Utils.zoom(400))

  override def paintComponent(g: Graphics): Unit = {
    image match {
      case None => super.paintComponent(g)
      case Some(image) =>
        val size: Int = Utils.zoom(400)

        g.asInstanceOf[Graphics2D].setRenderingHint(
          RenderingHints.KEY_RENDERING,
          RenderingHints.VALUE_RENDER_QUALITY)
        val ratio = image.getWidth(null) / image.getHeight(null).toDouble
        val (w, h) = ratio match {
          case 1.0          => (size, size)
          case r if r < 1.0 => ((size * r).toInt, size)
          case r if r > 1.0 => (size, (size / r).toInt)
          case _            => throw new IllegalStateException
        }
        val x = (size - w) / 2
        val y = (size - h) / 2
        if (ratio != 1.0) {
          g.setColor(Color.black)
          g.fillRect(0, 0, size, size)
        }
        g.drawImage(image, x, y, w, h, this)
    }
  }
}
