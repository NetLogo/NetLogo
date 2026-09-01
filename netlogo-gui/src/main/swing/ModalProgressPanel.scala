// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Color, Dimension }
import java.awt.event.{ MouseEvent, MouseListener }
import javax.swing.{ JLabel, JProgressBar, SwingConstants }

class ModalProgressPanel extends BoxRow(BoxAlign.Center) {
  private val label = new JLabel("", SwingConstants.CENTER)
  private val progressBar = new JProgressBar {
    setIndeterminate(true)
  }

  add(new BoxColumn(Seq(
    new BoxRow(label, BoxAlign.Center),
    progressBar
  ), 8, BoxAlign.Center) with PreferredSize {
    setOpaque(true)
    setBackground(Color.WHITE)
    setBorder(new ZoomableBorder(15, 20, 15, 20))
  })

  addMouseListener(new MouseListener {
    def mouseClicked(e: MouseEvent): Unit = {}
    def mouseEntered(e: MouseEvent): Unit = {}
    def mouseExited(e: MouseEvent): Unit = {}
    def mousePressed(e: MouseEvent): Unit = {}
    def mouseReleased(e: MouseEvent): Unit = {}
  })

  override def getPreferredSize: Dimension =
    getMaximumSize

  def setMessage(message: String): Unit = {
    label.setText(message)
  }
}
