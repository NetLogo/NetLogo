// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Frame, GridBagConstraints, GridBagLayout, Insets }
import java.io.File
import java.nio.file.Files
import java.util.prefs.{ Preferences => JavaPreferences }
import javax.swing.{ JLabel, JPanel }
import javax.swing.border.EmptyBorder

import org.nlogo.core.I18N
import org.nlogo.swing.{ ButtonPanel, DialogButton, FloatingTabbedPane, OptionPane, TabLabel, TextField, Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class PreferencesDialog(parent: Frame & ThemeSync, generalPreferences: Seq[Preference], codePreferences: Seq[Preference],
                        loggingPreferences: Seq[Preference]) extends ToolDialog(parent, "preferences") with ThemeSync {

  private lazy val netLogoPrefs = JavaPreferences.userRoot.node("/org/nlogo/NetLogo")

  private lazy val tabs = new FloatingTabbedPane

  private lazy val generalPreferencesPanel = new PreferenceContainer(generalPreferences)
  private lazy val codePreferencesPanel = new PreferenceContainer(codePreferences)
  private lazy val loggingPreferencesPanel = new PreferenceContainer(loggingPreferences)
  private lazy val themesPanel = new ThemesPanel(parent)

  private lazy val codeMessage = new JLabel(I18N.gui("code.message"))
  private lazy val loggingMessage = new JLabel(I18N.gui("logging.message"))

  private lazy val okButton = new DialogButton(true, I18N.gui.get("common.buttons.ok"), () => ok())
  private lazy val cancelButton = new DialogButton(false, I18N.gui.get("common.buttons.cancel"), () => cancel())

  override def setVisible(visible: Boolean): Unit = {
    if (visible)
      themesPanel.init()

    super.setVisible(visible)
  }

  // sync parameter prevents infinite recursion with syncTheme on load (Isaac B 5/22/25)
  private def reset(sync: Boolean): Unit = {
    generalPreferences.foreach(_.load(netLogoPrefs))
    codePreferences.foreach(_.load(netLogoPrefs))
    loggingPreferences.foreach(_.load(netLogoPrefs))

    themesPanel.revert(sync)
  }

  private def ok(): Unit = {
    if (apply())
      setVisible(false)
  }

  private def apply(): Boolean = {
    if (validatePrefs()) {
      generalPreferences.foreach(_.save(netLogoPrefs))
      codePreferences.foreach(_.save(netLogoPrefs))
      loggingPreferences.foreach(_.save(netLogoPrefs))

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
    if (loggingPreferences.find(x => x.i18nKey == "loggingEnabled").get.
        asInstanceOf[Preferences.BooleanPreference].component.isSelected) {
      val path = loggingPreferences.find(x => x.i18nKey == "logDirectory").get.
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
      generalPreferences.find(_.i18nKey == "uiScale").foreach(_.component.asInstanceOf[TextField].getText.toDouble)
    } catch {
      case e: NumberFormatException =>
        new OptionPane(this, I18N.gui.get("common.messages.error"), I18N.gui.get("tools.preferences.scaleError"),
                       OptionPane.Options.Ok, OptionPane.Icons.Error)
        return false
    }
    true
  }

  override def initGUI(): Unit = {
    val generalPreferencesContainer = new JPanel(new GridBagLayout) with Transparent {
      val c = new GridBagConstraints

      c.anchor = GridBagConstraints.NORTH
      c.weighty = 1
      c.insets = new Insets(24, 12, 24, 12)

      add(generalPreferencesPanel, c)
    }

    val codePreferencesContainer = new JPanel(new GridBagLayout) with Transparent {
      val c = new GridBagConstraints

      c.gridx = 0
      c.anchor = GridBagConstraints.NORTH
      c.insets = new Insets(24, 12, 24, 12)

      add(codeMessage, c)

      c.weighty = 1
      c.insets = new Insets(0, 12, 24, 12)

      add(codePreferencesPanel, c)
    }

    val loggingPreferencesContainer = new JPanel(new GridBagLayout) with Transparent {
      val c = new GridBagConstraints

      c.gridx = 0
      c.anchor = GridBagConstraints.NORTH
      c.insets = new Insets(24, 12, 24, 12)

      add(loggingMessage, c)

      c.weighty = 1
      c.insets = new Insets(0, 12, 24, 12)

      add(loggingPreferencesPanel, c)
    }

    val buttonPanel = new ButtonPanel(Seq(okButton, cancelButton))

    buttonPanel.setBorder(new EmptyBorder(6, 6, 6, 6))

    getRootPane.setDefaultButton(okButton)

    tabs.addTabWithLabel(generalPreferencesContainer, new TabLabel(tabs, I18N.gui("general"), generalPreferencesContainer))
    tabs.addTabWithLabel(codePreferencesContainer, new TabLabel(tabs, I18N.gui("code"), codePreferencesContainer))
    tabs.addTabWithLabel(loggingPreferencesContainer, new TabLabel(tabs, I18N.gui("logging"), loggingPreferencesContainer))
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

    generalPreferencesPanel.syncTheme()
    codePreferencesPanel.syncTheme()
    loggingPreferencesPanel.syncTheme()
    themesPanel.syncTheme()

    codeMessage.setForeground(InterfaceColors.dialogText())
    loggingMessage.setForeground(InterfaceColors.dialogText())
  }
}

private class PreferenceContainer(preferences: Seq[Preference])
  extends JPanel(new GridBagLayout) with Transparent with ThemeSync {

  private implicit val i18nPrefix: I18N.Prefix = I18N.Prefix("tools.preferences")

  val (labels, components) = preferences.foldLeft((Seq[JLabel](), Seq[ThemeSync]())) {
    case ((labels, components), pref) =>
      val c = new GridBagConstraints

      c.gridx = 0
      c.anchor = pref.anchor
      c.insets = new Insets(3, 0, 3, 6)

      val label = new JLabel(prefString(pref))

      add(label, c)

      c.gridx = 1
      c.insets = new Insets(3, 0, 3, 0)

      add(pref.component, c)

      (labels :+ label, components :+ pref.component)
  }

  private def prefString(pref: Preference): String =
    I18N.gui(pref.i18nKey) + pref.requirement.map(r => " " + I18N.gui(r.toString)).getOrElse("") + ":"

  override def syncTheme(): Unit = {
    labels.foreach(_.setForeground(InterfaceColors.dialogText()))
    components.foreach(_.syncTheme())
  }
}
