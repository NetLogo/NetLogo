// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import org.nlogo.swing.UserAction.MenuAction

trait MenuTab {
  val activeMenuActions = Seq[MenuAction]()
  val permanentMenuActions = Seq[MenuAction]()
}
