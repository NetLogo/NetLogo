// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ BorderLayout, Color, GridBagConstraints, GridBagLayout }
import javax.swing.{ JLabel, JPanel, JProgressBar, SwingConstants }
import javax.swing.border.EmptyBorder

class ModalProgressPanel extends JPanel(new GridBagLayout) {
  private val label = new JLabel("", SwingConstants.CENTER)
  private val progressBar = new JProgressBar {
    setIndeterminate(true)
  }

  private val panel = new JPanel(new BorderLayout(0, 8)) {
    setBorder(new EmptyBorder(15, 20, 15, 20))

    add(label, BorderLayout.NORTH)
    add(progressBar, BorderLayout.SOUTH)
  }

  setBackground(new Color(0, 0, 0, 128))
  setOpaque(false)

  add(panel, new GridBagConstraints)

  def setMessage(message: String): Unit = {
    label.setText(message)
  }
}
