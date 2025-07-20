// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import com.sun.jna.platform.win32.{ Advapi32Util, WinReg }

import java.awt.Color
import javax.swing.UIManager

object WindowsLookAndFeel {
  // the following code is necessary because the default FlatLaf settings override the Windows title bar colors,
  // ignoring the system accent color for any frames and dialogs created by the application. this code retrieves
  // the accent color from the registry (if the accent color has been  enabled for windows) and manually applies
  // it to all application windows, overriding the FlatLaf settings. (Isaac B 7/20/25)
  def setTitleBarColors(): Unit = {
    if (Advapi32Util.registryGetIntValue(WinReg.HKEY_CURRENT_USER,
                                         "Software\\Microsoft\\Windows\\DWM", "ColorPrevalence") != 0) {
      val accentColor = Advapi32Util.registryGetIntValue(WinReg.HKEY_CURRENT_USER,
                                                        "Software\\Microsoft\\Windows\\DWM", "AccentColor")

      val r = accentColor & 0xff
      val g = (accentColor >> 8) & 0xff
      val b = (accentColor >> 16) & 0xff
      val a = (accentColor >> 24) & 0xff

      UIManager.put("TitlePane.unifiedBackground", false)
      UIManager.put("TitlePane.background", new Color(r, g, b, a))
    }
  }
}
