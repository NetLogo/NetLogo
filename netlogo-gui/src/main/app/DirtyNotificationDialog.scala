// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.event.ActionEvent
import java.awt.{ BorderLayout, Component }
import javax.swing.{ AbstractAction, BorderFactory, JComponent, JButton, JDialog, JLabel, JPanel, SwingConstants }

import org.nlogo.awt.{ Hierarchy, Positioning }
import org.nlogo.core.I18N
import org.nlogo.swing.ButtonPanel

class DirtyNotificationDialog(owner: Component, okCallback: () => Unit, cancelCallback: () => Unit)
extends JDialog(Hierarchy.getFrame(owner), true)
{
  private val title: String = I18N.gui.get("dirty.dialog.title")
  private val message: String = I18N.gui.get("dirty.dialog.message")
  private val label: JLabel = new JLabel(message, SwingConstants.CENTER)
  private val panel: JPanel = new JPanel
  private val buttonsPanel = new ButtonPanel(makeButtons)

  setTitle(title)

  panel.setLayout(new BorderLayout(0, 8))
  panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20))
  panel.add(label, BorderLayout.NORTH)

  buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20))

  getContentPane.setLayout(new BorderLayout)
  getContentPane.add(panel, BorderLayout.CENTER)
  getContentPane.add(buttonsPanel, BorderLayout.SOUTH)

  pack()
  Positioning.center(this, Hierarchy.getFrame(owner))

  private class CancelAction extends AbstractAction(I18N.gui.get("common.buttons.cancel")) {
    def actionPerformed(e: ActionEvent) {
      setVisible(false)
      cancelCallback()
    }
  }

  private class OkAction extends AbstractAction(I18N.gui.get("common.buttons.ok")) {
    def actionPerformed(e: ActionEvent) {
      setVisible(false)
      okCallback()
    }
  }

  def makeButtons(): Seq[JComponent] = {
    val cancelButton = new JButton(new CancelAction)
    val okButton = new JButton(new OkAction)
    getRootPane.setDefaultButton(cancelButton)
    Seq(okButton, cancelButton)
  }
}

