// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

import java.awt.{ Component, Image, MediaTracker, Toolkit }
import java.awt.image.BufferedImage

object Images {

  def loadImage(image: Image): Boolean = {
    val mt = new MediaTracker(new Component() { })
    mt.addImage(image, 0)
    try mt.waitForAll()
    catch { case ex: InterruptedException => false }
    !mt.isErrorAny
  }

  def loadImageResource(path: String): Image = {
    val image = Toolkit.getDefaultToolkit.getImage(
      getClass.getResource(path))
    if (loadImage(image)) image else null
  }

  def loadImageFile(path: String, cache: Boolean): Image = {
    val image =
      if (cache)
        Toolkit.getDefaultToolkit.getImage(path)
      else
        Toolkit.getDefaultToolkit.createImage(path)
    if (loadImage(image)) image else null
  }

  def paintToImage(comp: Component): BufferedImage = {
    val image = new BufferedImage(comp.getWidth, comp.getHeight,
                                  BufferedImage.TYPE_INT_ARGB)
    // If we just call paint() here we get weird results on Windows; printAll appears to work
    // ev 5/13/09
    comp.printAll(image.getGraphics)
    image
  }

}
