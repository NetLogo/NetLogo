// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Frame }
import java.io.File
import java.util.prefs.{ Preferences => JavaPreferences }
import javax.swing.{ BorderFactory, Box, BoxLayout, SwingConstants }

import org.nlogo.core.I18N
import org.nlogo.swing.{ Button, OptionPane, TextFieldBox }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class PreferencesDialog(parent: Frame, preferences: Preference*) extends ToolDialog(parent, "preferences")
                                                                 with ThemeSync {
  private lazy val netLogoPrefs = JavaPreferences.userRoot.node("/org/nlogo/NetLogo")

  private lazy val preferencesPanel = new TextFieldBox(SwingConstants.TRAILING)

  private lazy val okButton = new Button(I18N.gui.get("common.buttons.ok"), ok)
  private lazy val applyButton = new Button(I18N.gui.get("common.buttons.apply"), apply)
  private lazy val cancelButton = new Button(I18N.gui.get("common.buttons.cancel"), cancel)

  private def reset() = {
    preferences foreach (_.load(netLogoPrefs))
  }
  private def ok() = {
    if (apply()) setVisible(false)
  }
  private def apply(): Boolean = {
    if (validatePrefs()) {
      preferences foreach (_.save(netLogoPrefs))
      return true
    }
    false
  }
  private def cancel() = {
    reset()
    setVisible(false)
  }
  private def validatePrefs(): Boolean = {
    if (preferences.find(x => x.i18nKey == "loggingEnabled").get.
        asInstanceOf[Preferences.BooleanPreference].component.isSelected) {
      val path = preferences.find(x => x.i18nKey == "logDirectory").get.
                 asInstanceOf[Preferences.LogDirectory].textField.getText
      val file = new File(path)
      if (path.nonEmpty && !file.exists) {
        if (new OptionPane(this, I18N.gui.get("common.messages.warning"),
                           I18N.gui.get("tools.preferences.missingDirectory"), OptionPane.Options.YesNo,
                           OptionPane.Icons.Warning).getSelectedIndex != 0)
          return false
        file.mkdirs
      }
    }
    true
  }

  override def initGUI() = {
    preferencesPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10))
    preferences.foreach(pref =>
      preferencesPanel.addField(
        (if (pref.requirement != RequiredAction.None) I18N.gui(pref.requirement.toString) + " " else "") +
        I18N.gui(pref.i18nKey), pref.component))

    val buttonsPanel = new Box(BoxLayout.LINE_AXIS)
    buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20))
    buttonsPanel.add(Box.createHorizontalGlue)
    buttonsPanel.add(okButton)
    buttonsPanel.add(Box.createHorizontalGlue)
    buttonsPanel.add(applyButton)
    buttonsPanel.add(Box.createHorizontalGlue)
    buttonsPanel.add(cancelButton)
    buttonsPanel.add(Box.createHorizontalGlue)

    add(preferencesPanel, BorderLayout.CENTER)
    add(buttonsPanel, BorderLayout.SOUTH)
    pack()

    reset()
    setResizable(false)
  }

  override def onClose() = reset()

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.DIALOG_BACKGROUND)
    preferencesPanel.setBackground(InterfaceColors.DIALOG_BACKGROUND)

    okButton.syncTheme()
    applyButton.syncTheme()
    cancelButton.syncTheme()

    preferencesPanel.syncTheme()

    preferences.foreach(_.component.syncTheme())
  }
}
