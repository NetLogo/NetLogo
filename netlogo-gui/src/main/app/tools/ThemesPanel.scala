// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ Frame, GridBagConstraints, GridBagLayout, Insets }
import javax.swing.{ ButtonGroup, JLabel, JPanel }

import org.nlogo.analytics.Analytics
import org.nlogo.core.{ I18N, NetLogoPreferences }
import org.nlogo.swing.{ RadioButton, Transparent }
import org.nlogo.theme.{ ClassicTheme, ColorTheme, DarkTheme, InterfaceColors, LightTheme, ThemeSync }

class ThemesPanel(frame: Frame & ThemeSync) extends JPanel(new GridBagLayout) with Transparent with ThemeSync {
  private implicit val i18nPrefix: I18N.Prefix = I18N.Prefix("tools.preferences.themes")

  private val label = new JLabel(s"<html>${I18N.gui("text")}</html>")

  private val systemButton = new RadioButton(I18N.gui("system"), () => setTheme(None))
  private val classicButton = new RadioButton(I18N.gui("classic"), () => setTheme(Some(ClassicTheme)))
  private val lightButton = new RadioButton(I18N.gui("light"), () => setTheme(Some(LightTheme)))
  private val darkButton = new RadioButton(I18N.gui("dark"), () => setTheme(Some(DarkTheme)))

  private var startTheme: Option[ColorTheme] = {
    NetLogoPreferences.get("colorTheme2", NetLogoPreferences.get("colorTheme", "system")) match {
      case "system" => None
      case "classic" => Some(ClassicTheme)
      case "light" => Some(LightTheme)
      case "dark" => Some(DarkTheme)
    }
  }

  locally {
    val c = new GridBagConstraints

    c.gridx = 0
    c.anchor = GridBagConstraints.NORTH
    c.insets = new Insets(24, 12, 24, 12)

    add(label, c)

    c.weighty = 1
    c.insets = new Insets(0, 12, 24, 12)

    add(new JPanel(new GridBagLayout) with Transparent {
      val c = new GridBagConstraints

      c.gridx = 0
      c.anchor = GridBagConstraints.WEST
      c.insets = new Insets(0, 6, 6, 6)

      add(systemButton, c)
      add(lightButton, c)
      add(darkButton, c)
      add(classicButton, c)
    }, c)

    val themeButtons = new ButtonGroup

    themeButtons.add(systemButton)
    themeButtons.add(classicButton)
    themeButtons.add(lightButton)
    themeButtons.add(darkButton)
  }

  def init(): Unit = {
    startTheme = {
      if (NetLogoPreferences.get("colorTheme2", null) == "system") {
        None
      } else {
        Some(InterfaceColors.getTheme)
      }
    }

    setSelected(startTheme)
  }

  // sync parameter prevents infinite recursion with syncTheme on load (Isaac B 5/22/25)
  def revert(sync: Boolean): Unit = {
    if (sync) {
      setTheme(startTheme)
    } else {
      InterfaceColors.setTheme(startTheme.getOrElse(InterfaceColors.systemTheme))
    }

    setSelected(startTheme)
  }

  private def setTheme(theme: Option[ColorTheme]): Unit = {
    InterfaceColors.setTheme(theme.getOrElse(InterfaceColors.systemTheme))

    val themeString = theme match {
      case Some(ClassicTheme) => "classic"
      case Some(LightTheme) => "light"
      case Some(DarkTheme) => "dark"
      case _ => "system"
    }

    if (themeString != NetLogoPreferences.get("colorTheme2", "system"))
      Analytics.preferenceChange("colorTheme2", themeString)

    NetLogoPreferences.put("colorTheme2", themeString)

    frame.syncTheme()
  }

  private def setSelected(theme: Option[ColorTheme]): Unit = {
    theme match {
      case Some(ClassicTheme) => classicButton.setSelected(true)
      case Some(LightTheme) => lightButton.setSelected(true)
      case Some(DarkTheme) => darkButton.setSelected(true)
      case _ => systemButton.setSelected(true)
    }
  }

  override def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.dialogText())

    systemButton.syncTheme()
    lightButton.syncTheme()
    darkButton.syncTheme()
    classicButton.syncTheme()
  }
}
