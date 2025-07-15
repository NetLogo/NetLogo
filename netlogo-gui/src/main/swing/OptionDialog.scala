// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Frame }
import javax.swing.{ JComboBox, JOptionPane }

import org.nlogo.awt.{ Hierarchy, LineBreaker }

@deprecated("use OptionPane instead for theme synchronization", "7.0")
class OptionDialog[T <: AnyRef](owner: Frame, title: String, message: String, choices: Array[T], i18n: String => String)
extends UserDialog(owner, title, i18n) {
  private val options = new JComboBox[T](choices)
  addComponents(options, message)

  def showOptionDialog(): AnyRef = {
    val r = getOwner.getBounds
    setLocation(r.x + (r.width  / 2) - (getWidth  / 2),
                r.y + (r.height / 2) - (getHeight / 2))
    setVisible(true)
    if (selection == 0) options.getSelectedIndex: Integer else null
  }
}

object OptionDialog {
  def showMessage(owner: Component, title: String, message: String, options: Array[? <: Object]): Int = {
    val brokenLines = LineBreaker.breakLines(message,
      owner.getFontMetrics(owner.getFont), UserDialog.DIALOG_WIDTH)
    showCustom(owner, title, brokenLines.mkString("\n"), options)
  }

  def showCustom(owner: Component, title: String, message: AnyRef, options: Array[? <: Object]): Int = {
    val parent: Component = Hierarchy.getWindow(owner)
    JOptionPane.showOptionDialog(parent, message, title,
      JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
      options.asInstanceOf[Array[Object]], options(0))
  }

  def showIgnoringCloseBox(owner: Component, title: String, message: String, options: Array[AnyRef], asList: Boolean) = {
    val showDialog = {
      val showFunction = if (asList) showAsList else showMessage
      () => showFunction(owner, title, message, options)
    }
    var result = -1
    while (result == -1)
      result = showDialog()
    result
  }

  def showAsList(owner: Component, title: String, message: String, options: Array[AnyRef]) =
    options.indexOf(JOptionPane.showInputDialog(
      Hierarchy.getFrame(owner), message, title,
      JOptionPane.QUESTION_MESSAGE, null, options, options(0)))
}
