// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Frame }
import java.io.File
import java.util.prefs.{ Preferences => JavaPreferences }
import javax.swing.{ BorderFactory, Box, BoxLayout, JButton, SwingConstants }
import javax.swing.border.EmptyBorder

import org.nlogo.core.I18N
import org.nlogo.swing.{ OptionDialog, RichAction, TextFieldBox }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.RoundedBorderPanel

class PreferencesDialog(parent: Frame, preferences: Preference*) extends ToolDialog(parent, "preferences")
                                                                 with ThemeSync {
  private lazy val netLogoPrefs = JavaPreferences.userRoot.node("/org/nlogo/NetLogo")

  private lazy val preferencesPanel = new TextFieldBox(SwingConstants.TRAILING)

  private lazy val okButton = new JButton(RichAction(I18N.gui.get("common.buttons.ok"))(_ => ok()))
    with RoundedBorderPanel {
    setBorder(new EmptyBorder(3, 12, 3, 12))
    setDiameter(6)
    enableHover()
  }

  private lazy val applyButton = new JButton(RichAction(I18N.gui.get("common.buttons.apply"))(_ => apply()))
    with RoundedBorderPanel {
    setBorder(new EmptyBorder(3, 12, 3, 12))
    setDiameter(6)
    enableHover()
  }

  private lazy val cancelButton = new JButton(RichAction(I18N.gui.get("common.buttons.cancel"))(_ => cancel()))
    with RoundedBorderPanel {
    setBorder(new EmptyBorder(3, 12, 3, 12))
    setDiameter(6)
    enableHover()
  }

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
    preferencesPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10))
    preferences.foreach(pref =>
      preferencesPanel.addField(
        (if (pref.requirement.nonEmpty) I18N.gui(pref.requirement) + " " else "") +
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

  def syncTheme() {
    getContentPane.setBackground(InterfaceColors.DIALOG_BACKGROUND)
    preferencesPanel.setBackground(InterfaceColors.DIALOG_BACKGROUND)

    okButton.setBackgroundColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
    okButton.setBackgroundHoverColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND_HOVER)
    okButton.setBorderColor(InterfaceColors.TOOLBAR_CONTROL_BORDER)
    okButton.setForeground(InterfaceColors.TOOLBAR_TEXT)

    applyButton.setBackgroundColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
    applyButton.setBackgroundHoverColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND_HOVER)
    applyButton.setBorderColor(InterfaceColors.TOOLBAR_CONTROL_BORDER)
    applyButton.setForeground(InterfaceColors.TOOLBAR_TEXT)

    cancelButton.setBackgroundColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
    cancelButton.setBackgroundHoverColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND_HOVER)
    cancelButton.setBorderColor(InterfaceColors.TOOLBAR_CONTROL_BORDER)
    cancelButton.setForeground(InterfaceColors.TOOLBAR_TEXT)

    preferencesPanel.syncTheme()

    preferences.foreach(_.component.syncTheme())
  }
}
