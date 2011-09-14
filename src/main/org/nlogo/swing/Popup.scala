package org.nlogo.swing

import javax.swing.{JDialog, JPanel}
import java.awt.{BorderLayout, Frame}
import org.nlogo.api.I18N

/**
 * Pops up the given panel as a modal window above the parent frame.
 * Adds OK and Cancel buttons that do
 * whatever you say to do in the given functions.
 */
class Popup(parentFrame: Frame, title:String, panel: JPanel, cancel: => Unit, ok: => Boolean, i18n: String => String) {
  val dialog = new JDialog(parentFrame, true)
  dialog.setTitle(title)
  dialog.add(panel, BorderLayout.CENTER)

  dialog.add(ButtonPanel(
    PimpedJButton(i18n("common.buttons.ok")){ if(ok) die() },
    PimpedJButton(i18n("common.buttons.cancel")){ cancel; die() }),
    BorderLayout.SOUTH)

  def show() {
    dialog.pack()
    org.nlogo.awt.Positioning.center(dialog, parentFrame)
    dialog.setVisible(true)
  }

  def die() {
    dialog.setVisible(false)
    dialog.dispose()
  }
}
