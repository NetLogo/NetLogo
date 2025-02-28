// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import com.formdev.flatlaf.{ FlatLaf, FlatLightLaf }

import java.awt.{ Color, Font, GraphicsEnvironment, Insets }
import javax.swing.{ UIManager, UnsupportedLookAndFeelException }

final object SetSystemLookAndFeel {

  /// Swing look & feel

  def setSystemLookAndFeel(): Unit = {
    try {
      setFlatLafLookAndFeel()
    } catch {
      case e: Throwable => println(e); setClassicNetLogoSystemLookAndFeel()
    }
  }

  def setClassicNetLogoSystemLookAndFeel(): Unit = {
    try {
      // this slider thing is a workaround for Java bug parade bug #6465237 - ST 1/20/09
      // Note: https://bugs.java.com/bugdatabase/view_bug?bug_id=6465237 says Closing as "not a defect".
      // so this is a permanent workaround - AAB 2/27/2024
      UIManager.put("Slider.paintValue", false)
      if (System.getProperty("os.name").startsWith("Mac")) {
        val lookAndFeel = System.getProperty("netlogo.swing.laf", UIManager.getSystemLookAndFeelClassName)
        UIManager.getDefaults.put("TabbedPane.foreground", new Color(0, 0, 0))
        UIManager.getDefaults.put("TabbedPane.selectedTabPadInsets", new Insets(0,0,-2,0))
        UIManager.getDefaults.put("TabbedPane.contentBorderInsets", new Insets(0,-10,-13,-9))
        // The java defaults specify this as black on my system,
        // which was distractingly bad for the name field of the "PlotPen" JTable when
        // that field was clicked, moved off and then released - RG 7/1/16
        UIManager.getDefaults.put("Table.focusCellBackground", new Color(202, 202, 202))
        UIManager.setLookAndFeel(lookAndFeel)
      } else if (System.getProperty("os.name").startsWith("Windows")) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
      } else if (System.getProperty("swing.defaultlaf") == null) {
        try {
          // On Linux (the likely system for this switch), or at least on Gnome, when dark
          // mode is enabled, NetLogo looks very wonky as it's not setup to properly
          // handle it with all its custom UI elements.  So we need to fall back to good
          // ol' Nimbus.  -Jeremy B September 2022 SEO: dark mode, dark theme
          UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")
        } catch {
          case _: Exception => UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
        }
      }
      // else do nothing, if swing.defaultlaf is set it should be respected.
      // this allows Linux/others users to set whatever L&F they want from
      // netlogo.sh - ST 12/15/09
    } catch {
      case ex @ (_:UnsupportedLookAndFeelException | _:IllegalAccessException |
                 _:ClassNotFoundException | _:InstantiationException) =>
        throw new IllegalStateException(ex)
    }
  }

  def setFlatLafLookAndFeel(): Unit = {
    // this slider thing is a workaround for Java bug parade bug #6465237 - ST 1/20/09
    UIManager.put("Slider.paintValue", false)
    // Hopefully we won't need this MacOS specific code
    // if we do turn it into a method to share with set classic laf
    if (System.getProperty("os.name").startsWith("Mac")) {
      // The java defaults specify this as black on my system,
      // which was distractingly bad for the name field of the "PlotPen" JTable when
      // that field was clicked, moved off and then released - RG 7/1/16
      UIManager.getDefaults.put("Table.focusCellBackground", new Color(202, 202, 202))
    }

    val font = Font.createFont(Font.TRUETYPE_FONT, getClass.getResourceAsStream("/fonts/OpenSans-Variable.ttf"))

    GraphicsEnvironment.getLocalGraphicsEnvironment.registerFont(font)

    FlatLaf.registerCustomDefaultsSource("themes")

    FlatLightLaf.setup()
  }
}
