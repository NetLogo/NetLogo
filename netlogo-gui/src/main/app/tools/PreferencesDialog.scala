// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Frame, GridBagConstraints, GridBagLayout, Insets }
import java.io.File
import java.nio.file.Files
import java.util.prefs.{ Preferences => JavaPreferences }
import javax.swing.{ JLabel, JPanel, SwingConstants }
import javax.swing.border.EmptyBorder

import org.nlogo.core.I18N
import org.nlogo.swing.{ ButtonPanel, DialogButton, FloatingTabbedPane, OptionPane, TabLabel, TextField, TextFieldBox,
                         Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class PreferencesDialog(parent: Frame & ThemeSync, preferences: Seq[Preference], codePreferences: Seq[Preference])
  extends ToolDialog(parent, "preferences") with ThemeSync {

  private lazy val netLogoPrefs = JavaPreferences.userRoot.node("/org/nlogo/NetLogo")

  private lazy val tabs = new FloatingTabbedPane

  private lazy val preferencesPanel = new TextFieldBox(SwingConstants.TRAILING)
  private lazy val codePreferencesContainer = new JPanel(new GridBagLayout) with Transparent
  private lazy val codePreferencesPanel = new TextFieldBox(SwingConstants.TRAILING)
  private lazy val themesPanel = new ThemesPanel(parent)

  private lazy val codeMessage = new JLabel(I18N.gui("code.message"))

  private lazy val okButton = new DialogButton(true, I18N.gui.get("common.buttons.ok"), () => ok())
  private lazy val cancelButton = new DialogButton(false, I18N.gui.get("common.buttons.cancel"), () => cancel())

  // sync parameter prevents infinite recursion with syncTheme on load (Isaac B 5/22/25)
  private def reset(sync: Boolean): Unit = {
    preferences.foreach(_.load(netLogoPrefs))
    codePreferences.foreach(_.load(netLogoPrefs))

    themesPanel.revert(sync)
  }

  private def ok(): Unit = {
    if (apply())
      setVisible(false)
  }

  private def apply(): Boolean = {
    if (validatePrefs()) {
      preferences.foreach(_.save(netLogoPrefs))
      codePreferences.foreach(_.save(netLogoPrefs))

      true
    } else {
      false
    }
  }

  private def cancel(): Unit = {
    reset(true)
    setVisible(false)
  }

  private def validatePrefs(): Boolean = {
    if (preferences.find(x => x.i18nKey == "loggingEnabled").get.
        asInstanceOf[Preferences.BooleanPreference].component.isSelected) {
      val path = preferences.find(x => x.i18nKey == "logDirectory").get.
                 asInstanceOf[Preferences.LogDirectory].textField.getText
      val file = new File(path)
      if (path.isEmpty) {
        new OptionPane(this, I18N.gui.get("common.messages.error"), I18N.gui.get("tools.preferences.emptyDirectory"),
                       OptionPane.Options.Ok, OptionPane.Icons.Error)
        return false
      }
      if (!file.exists) {
        if (new OptionPane(this, I18N.gui.get("common.messages.warning"),
                           I18N.gui.get("tools.preferences.missingDirectory"), OptionPane.Options.YesNo,
                           OptionPane.Icons.Warning).getSelectedIndex != 0)
          return false
        file.mkdirs
      }
      if (!Files.isWritable(file.toPath)) {
        new OptionPane(this, I18N.gui.get("common.messages.error"), I18N.gui.get("tools.preferences.badPermissions"),
                       OptionPane.Options.Ok, OptionPane.Icons.Error)
        return false
      }
    }
    try {
      preferences.find(_.i18nKey == "uiScale").foreach(_.component.asInstanceOf[TextField].getText.toDouble)
    } catch {
      case e: NumberFormatException =>
        new OptionPane(this, I18N.gui.get("common.messages.error"), I18N.gui.get("tools.preferences.scaleError"),
                       OptionPane.Options.Ok, OptionPane.Icons.Error)
        return false
    }
    true
  }

  override def initGUI() = {
    preferencesPanel.setBorder(new EmptyBorder(20, 10, 20, 10))

    preferences.foreach(pref =>
      preferencesPanel.addField(
        (if (pref.requirement != RequiredAction.None) I18N.gui(pref.requirement.toString) + " " else "") +
        I18N.gui(pref.i18nKey), pref.component))

    val c = new GridBagConstraints

    c.gridx = 0
    c.insets = new Insets(20, 10, 10, 10)

    codePreferencesContainer.add(codeMessage, c)

    codePreferences.foreach { pref =>
      codePreferencesPanel.addField(I18N.gui(pref.i18nKey), pref.component)
    }

    c.insets = new Insets(0, 10, 20, 10)

    codePreferencesContainer.add(codePreferencesPanel, c)

    val buttonPanel = new ButtonPanel(Seq(okButton, cancelButton))

    buttonPanel.setBorder(new EmptyBorder(6, 6, 6, 6))

    getRootPane.setDefaultButton(okButton)

    tabs.addTabWithLabel(preferencesPanel, new TabLabel(tabs, I18N.gui("general"), preferencesPanel))
    tabs.addTabWithLabel(codePreferencesContainer, new TabLabel(tabs, I18N.gui("code"), codePreferencesContainer))
    tabs.addTabWithLabel(themesPanel, new TabLabel(tabs, I18N.gui("themes"), themesPanel))

    add(tabs, BorderLayout.CENTER)
    add(buttonPanel, BorderLayout.SOUTH)

    pack()
    reset(false)

    setResizable(false)
  }

  override def onClose() = reset(true)

  def setSelectedIndex(index: Int): Unit = {
    tabs.setSelectedIndex(index)
  }

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground())

    okButton.syncTheme()
    cancelButton.syncTheme()

    preferencesPanel.syncTheme()
    codePreferencesPanel.syncTheme()
    themesPanel.syncTheme()

    preferences.foreach(_.component.syncTheme())
    codePreferences.foreach(_.component.syncTheme())

    codeMessage.setForeground(InterfaceColors.dialogText())
  }
}
