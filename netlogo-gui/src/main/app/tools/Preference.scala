// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.util.prefs.{ Preferences => JavaPreferences }
import javax.swing.JComponent

import org.nlogo.theme.ThemeSync

trait Preference {
  val i18nKey: String
  val component: JComponent with ThemeSync
  val requirement: String
  def load(prefs: JavaPreferences): Unit
  def save(prefs: JavaPreferences): Unit
}
