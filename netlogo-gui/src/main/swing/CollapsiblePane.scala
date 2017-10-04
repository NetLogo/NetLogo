// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.BorderLayout

import javax.swing.{ JComponent, JLabel, JPanel, JWindow, ImageIcon }
import javax.swing.border.LineBorder

class CollapsiblePane(element: JComponent, parent: JWindow) extends JPanel {
  private val open = new JLabel(new ImageIcon(
    classOf[CollapsiblePane].getResource("/images/popup.gif")))
  private val closed = new JLabel(new ImageIcon(
    classOf[CollapsiblePane].getResource("/images/closedarrow.gif")))

  locally {
    open.addMouseListener(
      new java.awt.event.MouseAdapter {
        override def mouseClicked(e: java.awt.event.MouseEvent) {
          setCollapsed(true) }})
    closed.addMouseListener(
      new java.awt.event.MouseAdapter {
        override def mouseClicked(e: java.awt.event.MouseEvent) {
          setCollapsed(false) }})
    setLayout(new BorderLayout)
    add(open,    BorderLayout.NORTH)
    add(element, BorderLayout.CENTER)
    setBorder(LineBorder.createGrayLineBorder)
  }

  def setCollapsed(collapsed: Boolean) {
    element.setVisible(!collapsed)
    if (collapsed) {
      remove(open)
      add(closed, BorderLayout.NORTH)
    } else {
      remove(closed)
      add(open, BorderLayout.NORTH)
    }
    parent.pack()
  }

  def isCollapsed = element.isVisible
}
