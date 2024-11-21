// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Frame

class InputDialog(owner: Frame, title: String, message: String, i18n: String => String, defaultInput: String = "")
extends UserDialog(owner, title, i18n) {
  private val field = new TextField(defaultInput, 0)
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
