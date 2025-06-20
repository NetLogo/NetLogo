// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Toolkit
import java.awt.datatransfer.{ DataFlavor, Transferable }
import java.util.ArrayList

import org.nlogo.core.Widget

import scala.jdk.CollectionConverters.{ ListHasAsScala, SeqHasAsJava }

object ClipboardUtils {
  private val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard

  // Seq is not serializable, which prevents cross-instance copy/paste, so use ArrayList instead (Isaac B 6/16/25)
  private val widgetsFlavor = new DataFlavor(classOf[ArrayList[Widget]], "NetLogo Widgets")

  def readWidgets(): Seq[Widget] = {
    if (clipboard.isDataFlavorAvailable(widgetsFlavor)) {
      clipboard.getData(widgetsFlavor).asInstanceOf[ArrayList[Widget]].asScala.toSeq
    } else {
      Seq()
    }
  }

  def writeWidgets(widgets: Seq[Widget]): Unit = {
    clipboard.setContents(new WidgetsTransferable(new ArrayList(widgets.asJava)), null)
  }

  def hasWidgets: Boolean =
    clipboard.isDataFlavorAvailable(widgetsFlavor)

  private class WidgetsTransferable(widgets: ArrayList[Widget]) extends Transferable {
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
