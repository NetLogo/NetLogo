// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

/**
 * This class is used to hold an image while on the clipboard.
 * Based on sample code from the Java Developer's Almanac:
 * http://javaalmanac.com/egs/java.awt.datatransfer/ToClipImg.html
 */

import java.awt.Image
import java.awt.datatransfer.{ DataFlavor, Transferable, UnsupportedFlavorException }

class ImageSelection(image: Image) extends Transferable {
  override def getTransferDataFlavors =
    Array(DataFlavor.imageFlavor)
  override def isDataFlavorSupported(flavor: DataFlavor) =
    flavor == DataFlavor.imageFlavor
  override def getTransferData(flavor: DataFlavor) = {
    if(!isDataFlavorSupported(flavor))
      throw new UnsupportedFlavorException(flavor)
    image
  }
}
