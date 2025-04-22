// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ BorderLayout, Frame }
import javax.swing.{ BorderFactory, JDialog, JLabel, JPanel, JProgressBar, SwingConstants }

import org.nlogo.awt.Positioning.center

class ModalProgressDialog(parent: Frame, message: String) extends JDialog(parent, true) {
  setResizable(false)
  setUndecorated(true)

  // make components
  val label = new JLabel(message, SwingConstants.CENTER)
  val progressBar = new JProgressBar
  progressBar.setIndeterminate(true)

  // lay out dialog
  val panel = new JPanel
  panel.setBorder(
    BorderFactory.createEmptyBorder(15, 20, 15, 20))

  panel.setLayout(new BorderLayout(0, 8))
  panel.add(label, BorderLayout.NORTH)
  panel.add(progressBar, BorderLayout.SOUTH)
  getContentPane.setLayout(new BorderLayout)
  getContentPane.add(panel, BorderLayout.CENTER)
  pack()
  center(this, parent)
}
