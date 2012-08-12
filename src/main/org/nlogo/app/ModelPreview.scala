// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Color, Dimension, Graphics, Graphics2D, Image, RenderingHints }

// extend JPanel not JComponent otherwise super.paintComponent() doesn't paint the background color
// for reasons I can't fathom - ST 8/3/03

class ModelPreview extends javax.swing.JPanel {

  setBackground(Color.BLACK)
  setOpaque(true)
  setPreferredSize(new Dimension(400, 400))
  setMinimumSize(getPreferredSize)
  setMaximumSize(getPreferredSize)

  private var image: Image = null

  def setImage(imagePath: String) {
    image = null
    if (imagePath != null)
      image = org.nlogo.awt.Images.loadImageFile(
        imagePath, false) // false = don't cache
    repaint()
  }

  override def paintComponent(g: Graphics) {
    if (image == null)
      super.paintComponent(g)
    else {
      g.asInstanceOf[Graphics2D].setRenderingHint(
        RenderingHints.KEY_RENDERING,
        RenderingHints.VALUE_RENDER_QUALITY)
      val width = image.getWidth(null)
      val height = image.getHeight(null)
      val (scaledWidth, scaledHeight) =
        width.compare(height) match {
          case 0 =>
            (400, 400)
          case 1 =>
            ((width * (400.0 / height)).toInt, 400)
          case -1 =>
            (400, (height * (400.0 / width)).toInt)
        }
      g.drawImage(image, 0, 0, scaledWidth, scaledHeight, this)
    }
  }

}
