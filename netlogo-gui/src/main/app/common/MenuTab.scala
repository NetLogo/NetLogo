// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import org.nlogo.swing.UserAction.MenuAction

trait MenuTab {
  def activeMenuActions = Seq[MenuAction]()
  def permanentMenuActions = Seq[MenuAction]()
}
