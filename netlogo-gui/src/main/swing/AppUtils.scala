// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import com.jthemedetecor.OsThemeDetector

import java.awt.GraphicsEnvironment

import org.nlogo.core.NetLogoPreferences
import org.nlogo.theme.{ ClassicTheme, DarkTheme, LightTheme, InterfaceColors }

// used by App and BehaviorSpaceApp to minimize duplicated GUI setup code and
// ensure visual unity between instances of the app (Isaac B 2/4/26)
object AppUtils {
  val defaultTheme: String = {
    NetLogoPreferences.get("colorTheme", {
      if (OsThemeDetector.getDetector.isDark) {
        "dark"
      } else {
        "light"
      }
    })
  }

  def setupGUI(colorTheme: Option[String]): Unit = {
    val scalePref = NetLogoPreferences.getDouble("uiScale", 1.0)

    if (scalePref > 1.0) {
      System.setProperty("sun.java2d.uiScale", scalePref.toString)

      Utils.setUIScale(scalePref)
    } else {
      val devices = GraphicsEnvironment.getLocalGraphicsEnvironment.getScreenDevices
      val scale = devices(0).getDefaultConfiguration.getDefaultTransform.getScaleX

      Utils.setUIScale(scale)
    }

    SetSystemLookAndFeel.setSystemLookAndFeel()

    InterfaceColors.setTheme(colorTheme.getOrElse(defaultTheme) match {
      case "classic" => ClassicTheme
      case "light" => LightTheme
      case "dark" => DarkTheme
    })

    System.setProperty("flatlaf.menuBarEmbedded", "false")
    System.setProperty("sun.awt.noerasebackground", "true") // stops view2.5d and 3d windows from blanking to white
  }
}
