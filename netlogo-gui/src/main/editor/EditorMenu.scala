// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import javax.swing.Action

trait EditorMenu {
  def offerAction(action: Action): Unit
}
