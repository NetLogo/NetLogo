// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ BorderLayout, Frame }
import java.awt.event.{ ActionEvent, KeyEvent }
import javax.swing.{ AbstractAction, JDialog, JPanel, KeyStroke }

import org.nlogo.core.LocaleContext
import org.nlogo.theme.InterfaceColors

/**
 * Pops up the given panel as a modal window above the parent frame.
 * Adds OK and Cancel buttons that do
 * whatever you say to do in the given functions.
 */
class Popup(parentFrame: Frame, title: String, panel: JPanel, cancel: => Unit, ok: => Boolean) {
  val dialog = new JDialog(parentFrame, true) with ZoomableWindow(Option(parentFrame))
  dialog.setTitle(title)
  dialog.add(panel, BorderLayout.CENTER)
  dialog.setAutoRequestFocus(true)
  dialog.getContentPane.setBackground(InterfaceColors.dialogBackground())

  private val okButton = new DialogButton(true, loc"OK", () => { if (ok) die() })

  private val buttonPanel = new ButtonPanel(
    Seq(okButton, new DialogButton(false, loc"Cancel", () => { cancel; die() }))) {
    setBorder(new ZoomableBorder(0, 0, 6, 0))
  }

  dialog.add(buttonPanel, BorderLayout.SOUTH)

  Utils.addEscKeyAction(dialog, new AbstractAction {
    override def actionPerformed(e: ActionEvent): Unit = {
      cancel
      die()
    }
  })

  dialog.getRootPane.getInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), new AbstractAction {
    override def actionPerformed(e: ActionEvent): Unit = {
      if (ok)
        die()
    }
  })

  dialog.getRootPane.setDefaultButton(okButton)

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
