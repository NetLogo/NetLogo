// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

/**
 * A JTextField which looks like a JLabel, but allows selection and dragging.
 */

import javax.swing.JTextField

// add spaces because (on windows) the left char might get cut if font is bold
class SelectableJLabel(str: String) extends JTextField(" " + str + " ") {
  setEditable(false)
  setOpaque(false) // so it's transparent
  setBorder(null) // get rid of the box around it
}
