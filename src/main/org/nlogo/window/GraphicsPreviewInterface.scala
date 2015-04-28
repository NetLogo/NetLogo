package org.nlogo.window

import javax.swing.JPanel

trait GraphicsPreviewInterface extends JPanel {
  def setImage(imagePath: String): Unit
  def setImage(newImage: java.awt.Image): Unit
}
