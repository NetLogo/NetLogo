// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.Frame
import java.io.File
import java.util.Locale
import java.util.prefs.{ Preferences => JavaPreferences }
import javax.swing.{ JCheckBox, JComboBox, JFileChooser, JPanel, JTextField }

import org.nlogo.app.common.TabsInterface
import org.nlogo.core.I18N
import org.nlogo.swing.RichJButton

object Preferences {
  abstract class BooleanPreference(val i18nKey: String, val restartRequired: Boolean, default: Boolean) extends Preference {
    val component = new JCheckBox

    def load(prefs: JavaPreferences) = {
      val value = prefs.get(i18nKey, default.toString).toBoolean
      component.setSelected(value)
    }

    def save(prefs: JavaPreferences) = {
      prefs.put(i18nKey, component.isSelected.toString)
    }
  }

  abstract class StringPreference(val i18nKey: String, val restartRequired: Boolean, default: String) extends Preference {
    val component = new JTextField(default, 20)

    def load(prefs: JavaPreferences) = {
      val value = prefs.get(i18nKey, default)
      component.setText(value)
    }

    def save(prefs: JavaPreferences) = {
      prefs.put(i18nKey, component.getText)
    }
  }

  object Language extends Preference {
    case class LocaleWrapper(val locale: Locale) {
      override def toString = locale.getDisplayName
    }

    val languages = I18N.availableLocales map (LocaleWrapper(_)) sortBy (_.toString)

    val i18nKey = "uiLanguage"
    val component = new JComboBox(languages)
    val restartRequired = true

    def load(prefs: JavaPreferences) = {
      val locale = I18N.localeFromPreferences.getOrElse(I18N.gui.defaultLocale)
      component.setSelectedItem(LocaleWrapper(locale))
    }
    def save(prefs: JavaPreferences) = {
      val chosenLocale = component.getSelectedItem.asInstanceOf[LocaleWrapper].locale
      prefs.put("user.language", chosenLocale.getLanguage)
      prefs.put("user.country", chosenLocale.getCountry)
    }
  }

  object LoadLastOnStartup extends BooleanPreference("loadLastOnStartup", false, false) {}

  class ReloadOnExternalChanges(tabs: TabsInterface) extends Preference {
    val i18nKey = "reloadOnExternalChanges"
    val component = new JCheckBox
    val restartRequired = false

    def load(prefs: JavaPreferences) = {
      val enabled = prefs.get("reloadOnExternalChanges", "false").toBoolean
      component.setSelected(enabled)
    }

    def save(prefs: JavaPreferences) = {
      val enabled = component.isSelected
      prefs.put("reloadOnExternalChanges", enabled.toString)
      tabs.watchingFiles = enabled
    }
  }

  class LineNumbers(tabs: TabsInterface) extends Preference {
    val i18nKey = "editorLineNumbers"
    val component = new JCheckBox
    val restartRequired = false

    def load(prefs: JavaPreferences) = {
      val lineNumsEnabled = prefs.get("line_numbers", "false").toBoolean
      component.setSelected(lineNumsEnabled)
    }

    def save(prefs: JavaPreferences) = {
      prefs.put("line_numbers", component.isSelected.toString)
      tabs.lineNumbersVisible = component.isSelected
    }
  }

  object IsLoggingEnabled extends BooleanPreference("loggingEnabled", true, false) {}

  class LogDirectory(val frame: Frame) extends Preference {
    val i18nKey         = "logDirectory"
    val restartRequired = true
    val textField       = new JTextField("", 20)
    val component       = createComponent()

    def load(prefs: JavaPreferences) = {
      val logDirectory = prefs.get("logDirectory", "")
      textField.setText(logDirectory)
    }

    def save(prefs: JavaPreferences) = {
      prefs.put("logDirectory", textField.getText)
    }

    def createComponent(): JPanel = {
      val editPanel = new JPanel
      editPanel.add(textField)
      editPanel.add(RichJButton("Browse...") {
        askForConfigFile(textField.getText).foreach(textField.setText)
      })
      editPanel
    }

    def askForConfigFile(current: String): Option[String] = {
      val dialog = new JFileChooser(new File(current))
      dialog.setDialogTitle("Log Directory")
      dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
      if (dialog.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
        val file = dialog.getSelectedFile
        val dir  = if (file.isDirectory) { file } else { dialog.getCurrentDirectory }
        val path = dir.getAbsolutePath
        Option(path)
      } else {
        None
      }
    }

  }

  object LogEvents extends StringPreference("logEvents", true, "")

  object IncludedFilesMenu  extends BooleanPreference("includedFilesMenu", true, false) {}

  object ProceduresMenuSortOrder extends Preference {
    val i18nKey = "proceduresMenuSortOrder"

    val options = Array(
      I18N.gui.get("tools.preferences.proceduresSortByOrderOfAppearance"),
      I18N.gui.get("tools.preferences.proceduresSortAlphabetical")
    )

    val component = new JComboBox(options)
    val restartRequired = false

    def load(prefs: JavaPreferences) = {
      val sortOrder = prefs.get("proceduresMenuSortOrder", options(0))
      component.setSelectedItem(sortOrder)
    }

    def save(prefs: JavaPreferences) = {
      val chosenSortOrder = component.getSelectedItem.asInstanceOf[String]
      prefs.put("proceduresMenuSortOrder", chosenSortOrder)
    }
  }

  object FocusOnError extends BooleanPreference("focusOnError", false, true) {}

  object StartSeparateCodeTab extends BooleanPreference("startSeparateCodeTab", false, false) {}
}
