// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.Frame
import javax.swing.{ ButtonGroup, JLabel }

import org.nlogo.analytics.Analytics
import org.nlogo.core.{ I18N, NetLogoPreferences }
import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow, PreferredSize, RadioButton, ZoomableBorder }
import org.nlogo.theme.{ ClassicTheme, ColorTheme, DarkTheme, InterfaceColors, LightTheme, ThemeSync }

class ThemesPanel(frame: Frame & ThemeSync) extends BoxColumn(24) with ThemeSync {
  private implicit val i18nPrefix: I18N.Prefix = I18N.Prefix("tools.preferences.themes")

  private val label = new JLabel(s"<html>${I18N.gui("text")}</html>") with PreferredSize

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

  setBorder(new ZoomableBorder(24, 12, 24, 12))

  add(new BoxRow(label, BoxAlign.Center))
  add(new BoxRow(new BoxColumn(Seq(
    systemButton,
    lightButton,
    darkButton,
    classicButton
  ), 6) with PreferredSize, BoxAlign.Center))

  new ButtonGroup {
    add(systemButton)
    add(classicButton)
    add(lightButton)
    add(darkButton)
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

  private [app] def scramble(): Unit = {
    if (lightButton.isSelected) {
      darkButton.setSelected(true)
    } else {
      lightButton.setSelected(true)
    }
  }
}
