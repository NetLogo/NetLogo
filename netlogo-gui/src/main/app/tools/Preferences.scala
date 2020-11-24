// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.io.File
import java.util.Locale
import java.util.prefs.{ Preferences => JavaPreferences }
import javax.swing.{ JCheckBox, JComboBox, JPanel, JTextField }
import java.awt.{ FileDialog, Frame }

import org.nlogo.app.common.TabsInterface
import org.nlogo.core.I18N
import org.nlogo.swing.RichJButton

object Preferences {
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

  object IsLoggingEnabled extends Preference {
    val i18nKey = "loggingEnabled"
    val component = new JCheckBox
    val restartRequired = true

    def load(prefs: JavaPreferences) = {
      val loggingEnabled = prefs.get("loggingEnabled", "false").toBoolean
      component.setSelected(loggingEnabled)
    }

    def save(prefs: JavaPreferences) = {
      prefs.put("loggingEnabled", component.isSelected.toString)
    }
  }

  class LoggingConfigFile(val frame: Frame) extends Preference {
    val i18nKey         = "loggingConfigFile"
    val restartRequired = true
    val textField       = new JTextField("", 20)
    val component       = createComponent()

    def load(prefs: JavaPreferences) = {
      val loggingConfigFile = prefs.get("loggingConfigFile", "")
      textField.setText(loggingConfigFile)
    }

    def save(prefs: JavaPreferences) = {
      prefs.put("loggingConfigFile", textField.getText)
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
      val dialog = new FileDialog(frame, "Logging Config File", FileDialog.LOAD)
      dialog.setDirectory(new File(current).getParent)
      dialog.setFile(new File(current).getName)
      dialog.setVisible(true)
      Option(dialog.getFile).map(Option(dialog.getDirectory).getOrElse("") + _)
    }

  }

  object IncludedFilesMenu extends Preference {
    val i18nKey = "includedFilesMenu"
    val component = new JCheckBox
    val restartRequired: Boolean = true

    def load(prefs: JavaPreferences) = {
      val alwaysVisible = prefs.get("includedFilesMenu", "false").toBoolean
      component.setSelected(alwaysVisible)
    }

    def save(prefs: JavaPreferences) = {
      prefs.put("includedFilesMenu", component.isSelected.toString)
    }
  }

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
}
