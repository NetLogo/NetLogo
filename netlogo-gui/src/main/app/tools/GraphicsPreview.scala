// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ Dimension, Graphics, Graphics2D, Image, RenderingHints }
import javax.swing.JPanel

import org.nlogo.awt.Images.loadImageFile
import org.nlogo.swing.{ PreferredSize, Transparent, Zoomable }
import org.nlogo.window.GraphicsPreviewInterface

// not JComponent otherwise super.paintComponent() doesn't paint the
// background color for reasons I can't fathom - ST 8/3/03
class GraphicsPreview extends JPanel with Transparent with GraphicsPreviewInterface with PreferredSize with Zoomable {
  private var image: Option[ImageInfo] = None

  def setImage(imagePath: String): Unit =
    setImage(Option(imagePath).map(loadImageFile(_, false)).orNull)

  def setImage(newImage: Image): Unit = {
    if (!image.exists(_ == newImage)) {
      image = Option(newImage).map(ImageInfo(_))
      repaint()
    }
  }

  override def getPreferredSize: Dimension = {
    image.fold(new Dimension(zoom(400), 0)) {
      case ImageInfo(_, width, height) =>
        new Dimension(zoom(width), zoom(height))
    }
  }

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)

    image match {
      case Some(ImageInfo(img, width, height)) =>
        g.asInstanceOf[Graphics2D].setRenderingHint(
          RenderingHints.KEY_RENDERING,
          RenderingHints.VALUE_RENDER_QUALITY)

        g.drawImage(img, 0, 0, zoom(width), zoom(height), this)

      case _ =>
    }
  }

  private object ImageInfo {
    def apply(image: Image): ImageInfo = {
      val width: Int = image.getWidth(null)
      val height: Int = image.getHeight(null)

      if (width >= height) {
        ImageInfo(image, 400, (400f * height / width).toInt)
      } else {
        ImageInfo(image, (400f * width / height).toInt, 400)
      }
    }
  }

  private case class ImageInfo(image: Image, width: Int, height: Int)
}
