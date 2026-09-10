// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import com.jthemedetecor.OsThemeDetector

import java.awt.GraphicsEnvironment

import org.nlogo.core.NetLogoPreferences
import org.nlogo.theme.{ ClassicTheme, DarkTheme, LightTheme, InterfaceColors }

import scala.sys.process.Process

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
    Option(System.getProperty("sun.java2d.uiScale")).flatMap(_.toFloatOption) match {
      case Some(scale) =>
        Utils.setUIScale(scale)

      case _ if System.getProperty("os.name").toLowerCase.startsWith("linux") =>
        try {
          val query: String = Process(Seq("xrdb", "-query")).!!

          """Xft\.dpi:\s*(\d+)""".r.findFirstMatchIn(query).flatMap(_.group(1).toIntOption).foreach { dpi =>
            Utils.setUIScale(dpi / 96f)
          }
        } catch {
          case _ =>
        }

      case _ =>
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
