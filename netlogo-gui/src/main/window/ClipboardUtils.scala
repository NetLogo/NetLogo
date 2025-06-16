// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Toolkit
import java.awt.datatransfer.{ Clipboard, ClipboardOwner, DataFlavor, Transferable }

import org.nlogo.core.Widget

object ClipboardUtils extends ClipboardOwner {
  private val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard

  private val widgetsFlavor = new DataFlavor(classOf[Seq[Widget]], "NetLogo Widgets")

  def readWidgets(): Seq[Widget] = {
    if (clipboard.isDataFlavorAvailable(widgetsFlavor)) {
      clipboard.getData(widgetsFlavor).asInstanceOf[Seq[Widget]]
    } else {
      Seq()
    }
  }

  def writeWidgets(widgets: Seq[Widget]): Unit = {
    clipboard.setContents(new WidgetsTransferable(widgets), this)
  }

  // no need to do anything here, no persistent state is stored (Isaac B 6/16/25)
  override def lostOwnership(clipboard: Clipboard, contents: Transferable): Unit = {}

  private class WidgetsTransferable(widgets: Seq[Widget]) extends Transferable {
    override def getTransferData(flavor: DataFlavor): AnyRef = {
      if (isDataFlavorSupported(flavor)) {
        widgets
      } else {
        null
      }
    }

    override def getTransferDataFlavors: Array[DataFlavor] =
      Array(widgetsFlavor)

    override def isDataFlavorSupported(flavor: DataFlavor): Boolean =
      getTransferDataFlavors.contains(flavor)
  }
}
