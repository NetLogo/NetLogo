// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Frame }
import java.io.File
import java.util.prefs.{ Preferences => JavaPreferences }
import javax.swing.{ BorderFactory, Box, BoxLayout, JButton, SwingConstants }

import org.nlogo.core.I18N
import org.nlogo.swing.{ OptionDialog, RichAction, TextFieldBox }

class PreferencesDialog(parent: Frame, preferences: Preference*)
extends ToolDialog(parent, "preferences") {
  private lazy val netLogoPrefs = JavaPreferences.userRoot.node("/org/nlogo/NetLogo")

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
        if (OptionDialog.showMessage(this, I18N.gui.get("common.messages.warning"),
                                              I18N.gui.get("tools.preferences.missingDirectory"),
                                              Array[Object](I18N.gui.get("common.buttons.ok"),
                                                            I18N.gui.get("common.buttons.cancel"))) == 1)
          return false
        file.mkdirs
      }
    }
    true
  }

  override def initGUI() = {
    val preferencesPanel = new TextFieldBox(SwingConstants.TRAILING)
    preferencesPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10))
    preferences foreach { pref =>
      val text = (if (pref.restartRequired) I18N.gui("restartRequired") + "  " else "") +
        I18N.gui(pref.i18nKey)
      preferencesPanel.addField(text, pref.component)
    }

    val buttonsPanel = new Box(BoxLayout.LINE_AXIS)
    buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20))
    val okAction = RichAction(I18N.gui.get("common.buttons.ok"))(_ => ok())
    val applyAction = RichAction(I18N.gui.get("common.buttons.apply"))(_ => apply())
    val cancelAction = RichAction(I18N.gui.get("common.buttons.cancel"))(_ => cancel())
    buttonsPanel.add(Box.createHorizontalGlue)
    buttonsPanel.add(new JButton(okAction))
    buttonsPanel.add(Box.createHorizontalGlue)
    buttonsPanel.add(new JButton(applyAction))
    buttonsPanel.add(Box.createHorizontalGlue)
    buttonsPanel.add(new JButton(cancelAction))
    buttonsPanel.add(Box.createHorizontalGlue)

    add(preferencesPanel, BorderLayout.CENTER)
    add(buttonsPanel, BorderLayout.SOUTH)
    pack()

    reset()
    setResizable(false)
  }

  override def onClose() = reset()
}
