// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Component, Font, Frame, GraphicsEnvironment, GridBagConstraints, GridBagLayout,
                  Insets }
import java.awt.event.ActionEvent
import java.io.File
import java.util.Locale
import javax.swing.{ AbstractAction, JComponent, JFileChooser, JLabel, JPanel }
import javax.swing.event.{ DocumentEvent, DocumentListener }

import org.nlogo.analytics.Analytics
import org.nlogo.app.common.TabsInterface
import org.nlogo.core.{ I18N, NetLogoPreferences }
import org.nlogo.swing.{ Button, CheckBox, ComboBox, TextField, Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.AbstractWidgetPanel
import org.nlogo.window.Events.AutoIndentEvent

object Preferences {
  abstract class BooleanPreference(i18nKey: String, requirement: Option[RequiredAction], default: Boolean)
    extends Preference(i18nKey, requirement) {

    val checkBox = new CheckBox("", selected => {
      if (selected != getPreference)
        Analytics.preferenceChange(i18nKey, selected.toString)

      onSelect(selected)
    })

    override def component: JComponent & ThemeSync =
      checkBox

    private def getPreference: Boolean =
      NetLogoPreferences.getBoolean(i18nKey, default)

    override def load(): Unit = {
      val pref = getPreference

      if (checkBox.isSelected != pref)
        checkBox.doClick()
    }

    override def save(): Unit = {
      NetLogoPreferences.putBoolean(i18nKey, checkBox.isSelected)
    }

    override def changed: Boolean =
      getPreference != checkBox.isSelected

    override def scramble(): Unit = {
      checkBox.doClick()
    }

    protected def onSelect(selected: Boolean): Unit = {}
  }

  abstract class StringPreference(i18nKey: String, requirement: Option[RequiredAction], default: String)
    extends Preference(i18nKey, requirement) {

    val textField = new TextField(20, default) {
      getDocument.addDocumentListener(new DocumentListener {
        override def changedUpdate(e: DocumentEvent): Unit = changed()
        override def insertUpdate(e: DocumentEvent): Unit = changed()
        override def removeUpdate(e: DocumentEvent): Unit = changed()
      })

      override def getInsets: Insets =
        new Insets(3, 3, 3, 0)

      private def changed(): Unit = {
        if (getText != getPreference)
          Analytics.preferenceChange(i18nKey, getText)
      }
    }

    override def component: JComponent & ThemeSync =
      textField

    private def getPreference: String =
      NetLogoPreferences.get(i18nKey, default)

    override def load(): Unit = {
      textField.setText(getPreference)
    }

    override def save(): Unit = {
      NetLogoPreferences.put(i18nKey, textField.getText)
    }

    override def changed: Boolean =
      getPreference != textField.getText
  }

  object Language extends Preference("uiLanguage", Some(RequiredAction.Restart), GridBagConstraints.NORTHWEST) {
    abstract sealed trait LocaleOption

    case object DetectLocale extends LocaleOption {
      override def toString = I18N.gui.get("tools.preferences.detectLanguage")
    }

    case class LocaleWrapper(val locale: Locale) extends LocaleOption {
      override def toString = locale.getDisplayLanguage(locale)
    }

    private val languages: Seq[LocaleOption] = DetectLocale +: I18N.availableLocales.distinctBy(_.getLanguage)
                                                                   .map(LocaleWrapper(_)).sortBy(_.toString).toSeq

    private val comboBox = new ComboBox(languages) {
      addItemListener(_ => {
        getSelectedItem match {
          case Some(DetectLocale) =>
            val text = Locale.getDefault.getDisplayLanguage

            label.setText(text)

            if (!getSelectedItem.contains(getPreference))
              Analytics.preferenceChange(i18nKey, text)

          case _ =>
            label.setText("")

            if (!getSelectedItem.contains(getPreference))
              Analytics.preferenceChange(i18nKey, "")
        }
      })
    }

    private val label = new JLabel

    private val panel = new JPanel(new GridBagLayout) with Transparent with ThemeSync {
      locally {
        val c = new GridBagConstraints

        c.gridx = 0
        c.fill = GridBagConstraints.HORIZONTAL
        c.weightx = 1
        c.insets = new Insets(0, 0, 3, 0)

        add(comboBox, c)

        c.anchor = GridBagConstraints.EAST
        c.fill = GridBagConstraints.NONE

        add(label, c)
      }

      override def syncTheme(): Unit = {
        label.setForeground(InterfaceColors.dialogText())

        comboBox.syncTheme()
      }
    }

    label.setFont(label.getFont.deriveFont(10f))

    override def component: JComponent & ThemeSync =
      panel

    private def getPreference: LocaleOption =
      I18N.localeFromPreferences.map(LocaleWrapper(_)).getOrElse(DetectLocale)

    override def load(): Unit = {
      comboBox.setSelectedItem(getPreference)
    }

    override def save(): Unit = {
      comboBox.getSelectedItem.foreach(_ match {
        case DetectLocale =>
          NetLogoPreferences.remove("user.language")
          NetLogoPreferences.remove("user.country")

        case LocaleWrapper(locale) =>
          NetLogoPreferences.put("user.language", locale.getLanguage)
          NetLogoPreferences.put("user.country", locale.getCountry)
      })
    }

    override def changed: Boolean =
      !comboBox.getSelectedItem.contains(getPreference)

    override def scramble(): Unit = {
      if (comboBox.getSelectedItem.contains(DetectLocale)) {
        comboBox.setSelectedIndex(1)
      } else {
        comboBox.setSelectedItem(DetectLocale)
      }
    }
  }

  object LoadLastOnStartup extends BooleanPreference("loadLastOnStartup", None, false)

  class ReloadOnExternalChanges(tabs: TabsInterface)
    extends BooleanPreference("reloadOnExternalChanges", None, false) {

    override def onSelect(selected: Boolean): Unit = {
      tabs.watchingFiles = selected
    }
  }

  object EnableRemoteCommands extends BooleanPreference("enableRemoteCommands", Some(RequiredAction.Restart), false)

  object IsLoggingEnabled extends BooleanPreference("loggingEnabled", Some(RequiredAction.Restart), false)

  class LogDirectory(val frame: Frame) extends StringPreference("logDirectory", Some(RequiredAction.Restart), "") {
    private val panel = new JPanel(new BorderLayout(6, 0)) with Transparent with ThemeSync {
      add(textField, BorderLayout.CENTER)

      private val browseButton = new Button(new AbstractAction("Browse...") {
        def actionPerformed(e: ActionEvent): Unit = {
          askForConfigFile(textField.getText).foreach(textField.setText)
        }
      })

      add(browseButton, BorderLayout.EAST)

      override def syncTheme(): Unit = {
        textField.syncTheme()
        browseButton.syncTheme()
      }
    }

    override def component: JComponent & ThemeSync =
      panel

    private def askForConfigFile(current: String): Option[String] = {
      val dialog = new JFileChooser(new File(current))

      dialog.setDialogTitle("Log Directory")
      dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)

      if (dialog.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
        val file = dialog.getSelectedFile

        if (file.isDirectory) {
          Option(file.getAbsolutePath)
        } else {
          Option(dialog.getCurrentDirectory.getAbsolutePath)
        }
      } else {
        None
      }
    }

    override def scramble(): Unit = {
      if (textField.getText.isEmpty) {
        textField.setText("/Users/stromboli/Documents/logs/")
      } else {
        textField.setText("")
      }
    }
  }

  object LogEvents extends StringPreference("logEvents", Some(RequiredAction.Restart), "") {
    override def scramble(): Unit = {
      if (textField.getText.isEmpty) {
        textField.setText("all")
      } else {
        textField.setText("")
      }
    }
  }

  class IncludedFilesMenu(tabs: TabsInterface) extends BooleanPreference("includedFilesMenu", None, true) {
    override def onSelect(selected: Boolean): Unit = {
      tabs.setIncludedFilesShown(selected)
    }
  }

  object ProceduresMenuSortOrder extends Preference("proceduresMenuSortOrder", None) {
    private val options = List(
      I18N.gui.get("tools.preferences.proceduresSortByOrderOfAppearance"),
      I18N.gui.get("tools.preferences.proceduresSortAlphabetical")
    )

    private val comboBox = new ComboBox(options) {
      addItemListener(_ => {
        if (!getSelectedItem.contains(getPreference))
          Analytics.preferenceChange(i18nKey, getSelectedItem.map(_.toString).orNull)
      })
    }

    override def component: JComponent & ThemeSync =
      comboBox

    private def getPreference: String =
      NetLogoPreferences.get(i18nKey, options(0))

    override def load(): Unit = {
      comboBox.setSelectedItem(getPreference)
    }

    override def save(): Unit = {
      comboBox.getSelectedItem.foreach(NetLogoPreferences.put(i18nKey, _))
    }

    override def changed: Boolean =
      !comboBox.getSelectedItem.contains(getPreference)

    override def scramble(): Unit = {
      if (comboBox.getSelectedIndex == 0) {
        comboBox.setSelectedIndex(1)
      } else {
        comboBox.setSelectedIndex(0)
      }
    }
  }

  object FocusOnError extends BooleanPreference("focusOnError", None, true)

  object StartSeparateCodeTab extends BooleanPreference("startSeparateCodeTab", None, false)

  class BoldWidgetText(widgetPanel: AbstractWidgetPanel) extends BooleanPreference("boldWidgetText", None, false) {
    override def onSelect(selected: Boolean): Unit = {
      widgetPanel.setBoldWidgetText(selected)
    }
  }

  object UIScale extends StringPreference("uiScale", Some(RequiredAction.Restart), "1.0") {
    override def scramble(): Unit = {
      if (textField.getText == "1.0") {
        textField.setText("2.0")
      } else {
        textField.setText("1.0")
      }
    }
  }

  class IndentAutomatically(raiser: Component) extends BooleanPreference("indentAutomatically", None, true) {
    override def onSelect(selected: Boolean): Unit = {
      new AutoIndentEvent(selected).raise(raiser)
    }
  }

  class EditorLineNumbers(tabs: TabsInterface) extends BooleanPreference("editorLineNumbers", None, true) {
    override def onSelect(selected: Boolean): Unit = {
      tabs.lineNumbersVisible = selected
    }
  }

  class JumpOnClick(tabs: TabsInterface) extends BooleanPreference("jumpOnClick", None, true) {
    override def onSelect(selected: Boolean): Unit = {
      tabs.setJumpOnClick(selected)
    }
  }

  object SendAnalytics extends BooleanPreference("sendAnalytics", None, false) {
    override def onSelect(selected: Boolean): Unit = {
      Analytics.refreshPreference()
    }
  }

  class CodeFont(tabs: TabsInterface) extends Preference("codeFont", None) {
    case class FontWrapper(font: Option[Font]) {
      override def toString: String =
        font.fold(I18N.gui.get("tools.preferences.defaultFont"))(_.getName)
    }

    val DefaultFont = FontWrapper(None)

    private val fonts: Seq[FontWrapper] = {
      DefaultFont +: GraphicsEnvironment.getLocalGraphicsEnvironment.getAvailableFontFamilyNames
        .filterNot(_.startsWith(".")).map(name => FontWrapper(Some(new Font(name, Font.PLAIN, 12)))).toSeq
    }

    private val comboBox = new ComboBox(fonts, false, true) {
      addItemListener(_ => {
        getSelectedItem.foreach { font =>
          if (font != getPreference)
            Analytics.preferenceChange(i18nKey, font.toString)

          tabs.setCodeFont(font.font)
        }
      })

      popup.getComponents.zip(fonts).foreach((c, f) => f.font.foreach(c.setFont))
    }

    override def component: JComponent & ThemeSync =
      comboBox

    private def getPreference: FontWrapper =
      FontWrapper(Option(NetLogoPreferences.get(i18nKey, null)).map(new Font(_, Font.PLAIN, 12)))

    override def load(): Unit = {
      comboBox.setSelectedItem(getPreference)
    }

    override def save(): Unit = {
      comboBox.getSelectedItem.foreach(_.font match {
        case Some(font) =>
          NetLogoPreferences.put(i18nKey, font.getName)

        case _ =>
          NetLogoPreferences.remove(i18nKey)
      })
    }

    override def changed: Boolean =
      !comboBox.getSelectedItem.contains(getPreference)

    override def scramble(): Unit = {
      if (comboBox.getSelectedIndex == 0) {
        comboBox.setSelectedIndex(1)
      } else {
        comboBox.setSelectedIndex(0)
      }
    }
  }
}
