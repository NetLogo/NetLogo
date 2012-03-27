// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

object FullScreenUtilities {
  def setWindowCanFullScreen(w: java.awt.Window, can: Boolean) {
    try
      Class.forName("com.apple.eawt.FullScreenUtilities")
        .getMethod("setWindowCanFullScreen",
                   classOf[java.awt.Window],
                   classOf[Boolean])
        .invoke(null, w, Boolean.box(can))
    catch { case e: Exception => } // ignore
  }
}
