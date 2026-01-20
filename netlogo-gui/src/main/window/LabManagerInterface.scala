// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.ModelSections.ModelSaveable
import org.nlogo.swing.UserAction.MenuAction
import org.nlogo.theme.ThemeSync
import org.nlogo.window.Event.LinkChild

trait LabManagerInterface extends LinkChild with ModelSaveable with ThemeSync {
  def show(): Unit
  def actions: Seq[MenuAction]
  def anyPaused: Boolean
}
