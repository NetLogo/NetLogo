// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo

import java.awt.image.BufferedImage

package object drawing {
  def imageToBytes(image: BufferedImage): Array[Byte] = {
    val outputStream = new java.io.ByteArrayOutputStream
    javax.imageio.ImageIO.write(image, "png", outputStream)
    outputStream.toByteArray
  }
  def cloneImage(image: BufferedImage): BufferedImage =
    new BufferedImage(image.getColorModel, image.copyData(null),
      image.getColorModel.isAlphaPremultiplied, null)
}
