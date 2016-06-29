// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.util.Locale
import java.util.prefs.Preferences
import javax.swing.{ JComboBox, JComponent }

import org.nlogo.core.I18N

trait Preference {
  val i18nKey: String
  val component: JComponent
  val restartRequired: Boolean
  def load(prefs: Preferences): Unit
  def save(prefs: Preferences): Unit
}

object Preference {
  object Language extends Preference {
    case class LocaleWrapper(val locale: Locale) {
      override def toString = locale.getDisplayName
    }

    val languages = I18N.availableLocales map (LocaleWrapper(_)) sortBy (_.toString)

    val i18nKey = "uiLanguage"
    val component = new JComboBox(languages)
    val restartRequired = true

    def load(prefs: Preferences) = {
      val locale = I18N.localeFromPreferences.getOrElse(I18N.gui.defaultLocale)
      component.setSelectedItem(LocaleWrapper(locale))
    }
    def save(prefs: Preferences) = {
      val chosenLocale = component.getSelectedItem.asInstanceOf[LocaleWrapper].locale
      prefs.put("user.language", chosenLocale.getLanguage)
      prefs.put("user.country", chosenLocale.getCountry)
    }
  }
}