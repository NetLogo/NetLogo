// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ BorderLayout, Color, GridBagConstraints, GridBagLayout }
import java.awt.event.{ MouseEvent, MouseListener }
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

  setOpaque(false)

  add(panel, new GridBagConstraints)

  addMouseListener(new MouseListener {
    def mouseClicked(e: MouseEvent): Unit = {}
    def mouseEntered(e: MouseEvent): Unit = {}
    def mouseExited(e: MouseEvent): Unit = {}
    def mousePressed(e: MouseEvent): Unit = {}
    def mouseReleased(e: MouseEvent): Unit = {}
  })

  def setMessage(message: String): Unit = {
    label.setText(message)
  }
}
