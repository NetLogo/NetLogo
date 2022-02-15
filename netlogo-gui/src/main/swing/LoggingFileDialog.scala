package org.nlogo.swing

import java.awt.{ Frame }

object LoggingFileDialog {
  var enableMitigation: Boolean = false
}

class LoggingFileDialog(w: Frame, t: String, m: Int) extends java.awt.FileDialog(w, t, m) {

  override def show() = {
    println("FileDialog.show()")
    Thread.dumpStack()
    super.show()
    println("FileDialog.show() complete.")
  }

  override def hide() = {
    println("FileDialog.hide()")
    Thread.dumpStack
    super.hide()
    println("FileDialog.hide() complete.")
  }

  override def setVisible(visible: Boolean) = {
    println(s"FileDialog.setVisible(${visible})")
    Thread.dumpStack()
    super.setVisible(visible)
    println(s"FileDialog.setVisible(${visible}) complete.")
  }

  override def dispose() = {
    println("FileDialog.dispose()")
    Thread.dumpStack()
    super.dispose()
    println("FileDialog.dispose() complete.")
  }

  override def isFocusable() = {
    println("FileDialog.isFocusable()")
    Thread.dumpStack()
    if (LoggingFileDialog.enableMitigation) { Thread.sleep(1000) }
    val retVal = super.isFocusable()
    println("FileDialog.isFocusable() complete.")
    retVal
  }

  override def setLocationByPlatform(locationByPlatform: Boolean) = {
    println(s"FileDialog.setLocationByPlatform(${locationByPlatform})")
    Thread.dumpStack()
    if (LoggingFileDialog.enableMitigation) { Thread.sleep(1000) }
    super.setLocationByPlatform(locationByPlatform)
    println(s"FileDialog.setLocationByPlatform(${locationByPlatform}) complete.")
  }

}
