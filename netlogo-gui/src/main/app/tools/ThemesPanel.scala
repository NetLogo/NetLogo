// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ Frame, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.ActionEvent
import java.util.prefs.{ Preferences => JavaPreferences }
import javax.swing.{ AbstractAction, ButtonGroup, JLabel, JPanel }

import org.nlogo.core.I18N
import org.nlogo.swing.{ RadioButton, Transparent }
import org.nlogo.theme.{ ClassicTheme, ColorTheme, DarkTheme, InterfaceColors, LightTheme, ThemeSync }

class ThemesPanel(frame: Frame & ThemeSync) extends JPanel(new GridBagLayout) with Transparent with ThemeSync {
  private implicit val i18nPrefix: I18N.Prefix = I18N.Prefix("tools.preferences.themes")

  private val prefs = JavaPreferences.userRoot.node("/org/nlogo/NetLogo")

  private val label = new JLabel(s"<html>${I18N.gui("text")}</html>")

  private val classicButton = new RadioButton(new AbstractAction(I18N.gui("classic")) {
    def actionPerformed(e: ActionEvent): Unit = {
      setTheme(ClassicTheme)
    }
  })

  private val lightButton = new RadioButton(new AbstractAction(I18N.gui("light")) {
    def actionPerformed(e: ActionEvent): Unit = {
      setTheme(LightTheme)
    }
  })

  private val darkButton = new RadioButton(new AbstractAction(I18N.gui("dark")) {
    def actionPerformed(e: ActionEvent): Unit = {
      setTheme(DarkTheme)
    }
  })

  private var startTheme: ColorTheme = InterfaceColors.getTheme

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

      add(lightButton, c)
      add(darkButton, c)
      add(classicButton, c)
    }, c)

    val themeButtons = new ButtonGroup

    themeButtons.add(classicButton)
    themeButtons.add(lightButton)
    themeButtons.add(darkButton)
  }

  def init(): Unit = {

    setSelected(startTheme)
  }

  // sync parameter prevents infinite recursion with syncTheme on load (Isaac B 5/22/25)
  def revert(sync: Boolean): Unit = {
    if (sync) {
      setTheme(startTheme)
    } else {
      InterfaceColors.setTheme(startTheme)
    }

    setSelected(startTheme)
  }

  private def setTheme(theme: ColorTheme): Unit = {
    InterfaceColors.setTheme(theme)

    prefs.put("colorTheme", theme match {
      case ClassicTheme => "classic"
      case LightTheme => "light"
      case DarkTheme => "dark"
      case _ => throw new IllegalStateException
    })

    frame.syncTheme()
  }

  private def setSelected(theme: ColorTheme): Unit = {
    theme match {
      case ClassicTheme => classicButton.setSelected(true)
      case LightTheme => lightButton.setSelected(true)
      case DarkTheme => darkButton.setSelected(true)
      case _ =>
    }
  }

  override def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.dialogText())

    lightButton.syncTheme()
    darkButton.syncTheme()
    classicButton.syncTheme()
  }
}
