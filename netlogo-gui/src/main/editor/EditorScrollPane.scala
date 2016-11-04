// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import javax.swing.JScrollPane

trait EditorScrollPane extends JScrollPane {
  def setLineNumbersEnabled(enabled: Boolean)
  def lineNumbersEnabled: Boolean
}
