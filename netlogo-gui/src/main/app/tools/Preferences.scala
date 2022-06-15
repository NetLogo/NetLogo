// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.util.Locale
import java.util.prefs.{ Preferences => JavaPreferences }
import javax.swing.{ JCheckBox, JComboBox }

import org.nlogo.app.common.TabsInterface
import org.nlogo.core.I18N

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
}
