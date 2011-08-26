package org.nlogo.awt

import java.awt.Toolkit
import java.awt.datatransfer.{ DataFlavor, Transferable, UnsupportedFlavorException }

object Clipboard {
  def getClipboardAsString(requester: AnyRef): String = {
    val kit = Toolkit.getDefaultToolkit
    val clipboard = kit.getSystemClipboard
    val transferable = clipboard.getContents(requester)
    def getString(t: Transferable) =
      t.getTransferData(DataFlavor.stringFlavor).asInstanceOf[String]
    try Option(transferable).map(getString).orNull
    catch {
      case _: java.io.IOException | _: UnsupportedFlavorException =>
        null
    }
  }
}
