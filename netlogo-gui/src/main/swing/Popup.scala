// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ BorderLayout, Frame }
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, JDialog, JPanel }

import org.nlogo.theme.InterfaceColors

/**
 * Pops up the given panel as a modal window above the parent frame.
 * Adds OK and Cancel buttons that do
 * whatever you say to do in the given functions.
 */
class Popup(parentFrame: Frame, title:String, panel: JPanel, cancel: => Unit, ok: => Boolean, i18n: String => String) {
  val dialog = new JDialog(parentFrame, true)
  dialog.setTitle(title)
  dialog.add(panel, BorderLayout.CENTER)
  dialog.setAutoRequestFocus(true)
  dialog.getContentPane.setBackground(InterfaceColors.dialogBackground())

  dialog.add(new ButtonPanel(Seq(
    new DialogButton(true, i18n("common.buttons.ok"), () => { if (ok) die() }),
    new DialogButton(false, i18n("common.buttons.cancel"), () => { cancel; die() }))),
    BorderLayout.SOUTH)

  Utils.addEscKeyAction(dialog, new AbstractAction {
    override def actionPerformed(e: ActionEvent): Unit = {
      cancel
      die()
    }
  })

  DialogForegrounder(dialog)

  def show(): Unit = {
    dialog.pack()
    org.nlogo.awt.Positioning.center(dialog, parentFrame)
    dialog.setVisible(true)
  }

  def die(): Unit = {
    dialog.setVisible(false)
    dialog.dispose()
  }
}
