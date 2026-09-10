// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import javax.swing.JComponent

import org.nlogo.theme.ThemeSync

// stores I18N key for hint to be added before preference name (Isaac B 2/9/25)
sealed trait RequiredAction

object RequiredAction {
  case object Restart extends RequiredAction {
    override def toString: String = "restartRequired"
  }
}

trait Preference(val i18nKey: String, val requirement: Option[RequiredAction], val top: Boolean = false) {
  def component: JComponent & ThemeSync
  def load(): Unit
  def save(): Unit
  def changed: Boolean
  private [app] def scramble(): Unit
}
