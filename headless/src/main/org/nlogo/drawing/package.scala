package org.nlogo

package object drawing {
  def imageToBytes(image: java.awt.image.BufferedImage): Array[Byte] = {
    val outputStream = new java.io.ByteArrayOutputStream
    javax.imageio.ImageIO.write(image, "png", outputStream)
    outputStream.toByteArray
  }
}