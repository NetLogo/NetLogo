// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.Frame
import java.awt.event.ActionEvent
import java.io.File
import java.util.Locale
import java.util.prefs.{ Preferences => JavaPreferences }
import javax.swing.{ AbstractAction, JComponent, JFileChooser, JPanel }

import org.nlogo.app.common.TabsInterface
import org.nlogo.core.I18N
import org.nlogo.swing.{ Button, CheckBox, ComboBox, TextField, Transparent }
import org.nlogo.theme.ThemeSync

object Preferences {
  abstract class BooleanPreference(val i18nKey: String, val requirement: RequiredAction, default: Boolean) extends Preference {
    private val checkBox = new CheckBox

    override def component: CheckBox = checkBox

    def load(prefs: JavaPreferences) = {
      val value = prefs.get(i18nKey, default.toString).toBoolean
      checkBox.setSelected(value)
    }

    def save(prefs: JavaPreferences) = {
      prefs.put(i18nKey, checkBox.isSelected.toString)
    }
  }

  abstract class StringPreference(val i18nKey: String, val requirement: RequiredAction, default: String) extends Preference {
    val textField = new TextField(20, default)

    def component: JComponent & ThemeSync = textField

    def load(prefs: JavaPreferences) = {
      val value = prefs.get(i18nKey, default)
      textField.setText(value)
    }

    def save(prefs: JavaPreferences) = {
      prefs.put(i18nKey, textField.getText)
    }
  }

  object Language extends Preference {
    case class LocaleWrapper(val locale: Locale) {
      override def toString = locale.getDisplayName
    }

    val languages = I18N.availableLocales map (LocaleWrapper(_)) sortBy (_.toString)

    val i18nKey = "uiLanguage"
    val comboBox = new ComboBox(languages.toList)
    val requirement = RequiredAction.Restart

    def component: JComponent & ThemeSync = comboBox

    def load(prefs: JavaPreferences): Unit = {
      val locale = I18N.localeFromPreferences.getOrElse(I18N.gui.defaultLocale)
      comboBox.setSelectedItem(LocaleWrapper(locale))
    }

    def save(prefs: JavaPreferences): Unit = {
      comboBox.getSelectedItem.foreach { w =>
        prefs.put("user.language", w.locale.getLanguage)
        prefs.put("user.country", w.locale.getCountry)
      }
    }
  }

  object LoadLastOnStartup extends BooleanPreference("loadLastOnStartup", RequiredAction.None, false) {}

  class ReloadOnExternalChanges(tabs: TabsInterface) extends Preference {
    val i18nKey = "reloadOnExternalChanges"
    val checkBox = new CheckBox
    val requirement = RequiredAction.None

    def component: JComponent & ThemeSync = checkBox

    def load(prefs: JavaPreferences) = {
      val enabled = prefs.get("reloadOnExternalChanges", "false").toBoolean
      checkBox.setSelected(enabled)
    }

    def save(prefs: JavaPreferences) = {
      val enabled = checkBox.isSelected
      prefs.put("reloadOnExternalChanges", enabled.toString)
      tabs.watchingFiles = enabled
    }
  }

  object IsLoggingEnabled extends BooleanPreference("loggingEnabled", RequiredAction.Restart, false) {}

  class LogDirectory(val frame: Frame) extends Preference {
    val i18nKey         = "logDirectory"
    val requirement = RequiredAction.Restart
    val textField       = new TextField(20)
    val component =
      new JPanel with Transparent with ThemeSync {
        add(textField)

        private val browseButton = new Button(new AbstractAction("Browse...") {
          def actionPerformed(e: ActionEvent): Unit = {
            askForConfigFile(textField.getText).foreach(textField.setText)
          }
        })

        add(browseButton)

        override def syncTheme(): Unit = {
          textField.syncTheme()
          browseButton.syncTheme()
        }
      }

    def load(prefs: JavaPreferences) = {
      val logDirectory = prefs.get("logDirectory", "")
      textField.setText(logDirectory)
    }

    def save(prefs: JavaPreferences) = {
      prefs.put("logDirectory", textField.getText)
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

  object LogEvents extends StringPreference("logEvents", RequiredAction.Restart, "")

  object IncludedFilesMenu  extends BooleanPreference("includedFilesMenu", RequiredAction.Restart, false) {}

  object ProceduresMenuSortOrder extends Preference {
    val i18nKey = "proceduresMenuSortOrder"

    val options = List(
      I18N.gui.get("tools.preferences.proceduresSortByOrderOfAppearance"),
      I18N.gui.get("tools.preferences.proceduresSortAlphabetical")
    )

    val comboBox = new ComboBox(options)
    val requirement = RequiredAction.None

    def component: JComponent & ThemeSync = comboBox

    def load(prefs: JavaPreferences): Unit = {
      val sortOrder = prefs.get("proceduresMenuSortOrder", options(0))
      comboBox.setSelectedItem(sortOrder)
    }

    def save(prefs: JavaPreferences): Unit = {
      comboBox.getSelectedItem.foreach(prefs.put("proceduresMenuSortOrder", _))
    }
  }

  object FocusOnError extends BooleanPreference("focusOnError", RequiredAction.None, true) {}

  object StartSeparateCodeTab extends BooleanPreference("startSeparateCodeTab", RequiredAction.None, false) {}

  object BoldWidgetNames extends BooleanPreference("boldWidgetNames", RequiredAction.Reload, false) {}

  object UIScale extends Preference {
    val i18nKey = "uiScale"
    val requirement = RequiredAction.Restart
    val textField = new TextField(20, "1.0")

    def component: JComponent & ThemeSync = textField

    def load(prefs: JavaPreferences) = {
      textField.setText(prefs.getDouble(i18nKey, 1.0).toString)
    }

    def save(prefs: JavaPreferences) = {
      prefs.putDouble(i18nKey, textField.getText.toDouble)
    }
  }
}
