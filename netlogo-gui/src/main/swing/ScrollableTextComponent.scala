// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import javax.swing.JScrollPane
import javax.swing.text.JTextComponent

// used by FindDialog to center selected text (Isaac B 7/26/25)
trait ScrollableTextComponent extends JTextComponent {
  def scrollPane: Option[JScrollPane]
}
