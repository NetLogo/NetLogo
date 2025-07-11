// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import org.nlogo.swing.UserAction.MenuAction

trait EditorMenu {
  def offerAction(action: MenuAction): Unit
}
