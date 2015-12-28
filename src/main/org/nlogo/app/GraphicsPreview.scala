package org.nlogo.app

class GraphicsPreview
  extends javax.swing.JPanel
  // not JComponent otherwise super.paintComponent() doesn't paint the
  // background color for reasons I can't fathom - ST 8/3/03
  with org.nlogo.window.GraphicsPreviewInterface {

  private var image: java.awt.Image = null

  setBackground(java.awt.Color.BLACK)
  setOpaque(true)
  setPreferredSize(new java.awt.Dimension(400, 400))
  setMinimumSize(getPreferredSize)
  setMaximumSize(getPreferredSize)

  def setImage(imagePath: String) {
    image = null
    if (imagePath != null) {
      image = org.nlogo.awt.Images.loadImageFile(imagePath, false)
    }
    repaint()
  }

  def setImage(newImage: java.awt.Image) {
    image = newImage
    repaint()
  }

  override def paintComponent(g: java.awt.Graphics) {
    if (image == null) {
      super.paintComponent(g)
    } else {
      g.asInstanceOf[java.awt.Graphics2D].setRenderingHint(
        java.awt.RenderingHints.KEY_RENDERING,
        java.awt.RenderingHints.VALUE_RENDER_QUALITY)
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
