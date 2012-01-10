// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Frame }
import org.nlogo.awt.Hierarchy.getFrame
import javax.swing.JOptionPane

object InputDialog {
  def show(owner: Component, title: String, message: String, defaultInput: String, options: Array[String]): String =
    javax.swing.JOptionPane.showInputDialog(
      getFrame(owner), message, title, JOptionPane.QUESTION_MESSAGE,
      null, null, defaultInput)
}

class InputDialog(owner: Frame, title: String, message: String, i18n: String => String)
extends UserDialog(owner, title, i18n) {
  private val field = new javax.swing.JTextField
  addComponents(field, message)
  def showInputDialog(): String = {
    val r = getOwner.getBounds
    setLocation(
      r.x + (r.width / 2) - (getWidth / 2),
      r.y + (r.height / 2) - (getHeight / 2))
    setVisible(true)
    if (selection == 0)
      field.getText
    else
      null
  }
}
