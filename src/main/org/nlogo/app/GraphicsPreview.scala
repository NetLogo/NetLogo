package org.nlogo.app

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.RenderingHints

import org.nlogo.awt.Images.loadImageFile
import org.nlogo.window.GraphicsPreviewInterface

import javax.swing.JPanel

class GraphicsPreview
  extends JPanel
  // not JComponent otherwise super.paintComponent() doesn't paint the
  // background color for reasons I can't fathom - ST 8/3/03
  with GraphicsPreviewInterface {

  private var image: Image = null

  setBackground(Color.BLACK)
  setOpaque(true)
  setPreferredSize(new Dimension(400, 400))
  setMinimumSize(getPreferredSize)
  setMaximumSize(getPreferredSize)

  def setImage(imagePath: String) {
    image = null
    if (imagePath != null) {
      image = loadImageFile(imagePath, false)
    }
    repaint()
  }

  def setImage(newImage: Image) {
    image = newImage
    repaint()
  }

  override def paintComponent(g: Graphics) {
    if (image == null) {
      super.paintComponent(g)
    } else {
      g.asInstanceOf[Graphics2D].setRenderingHint(
        RenderingHints.KEY_RENDERING,
        RenderingHints.VALUE_RENDER_QUALITY)
      val width = image.getWidth(null)
      val height = image.getHeight(null)
      if (width == height) {
        g.drawImage(image, 0, 0, 400, 400, this)
      } else if (width > height) {
        g.drawImage(image, 0, 0, (width * (400.0 / height)).toInt, 400, this)
      } else {
        g.drawImage(image, 0, 0, 400, (height * (400.0 / width)).toInt, this)
      }
    }
  }
}
