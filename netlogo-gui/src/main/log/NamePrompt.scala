// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import javax.swing.{ JFrame, JOptionPane }

import org.nlogo.core.I18N

object NamePrompt {

  def ask() = {
    val frame = new JFrame()
    frame.setAlwaysOnTop(true)
    val prompt = I18N.gui.get("tools.loggingMode.enterName")
    val name   = JOptionPane.showInputDialog(frame, prompt, "", JOptionPane.QUESTION_MESSAGE, null, null, "")
    if (name == null) { "unknown" } else { name.toString.trim() }
  }

}
