// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

class CollapsiblePane(element: javax.swing.JComponent, parent: javax.swing.JWindow)
extends javax.swing.JPanel {

  private val open =
    new org.nlogo.swing.IconHolder(
      new javax.swing.ImageIcon(
        classOf[CollapsiblePane].getResource("/images/popup.gif")))
  private val closed =
    new org.nlogo.swing.IconHolder(
      new javax.swing.ImageIcon(
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
    setLayout(new java.awt.BorderLayout())
    add(open, java.awt.BorderLayout.NORTH)
    add(element, java.awt.BorderLayout.CENTER)
    setBorder(javax.swing.border.LineBorder.createGrayLineBorder())
  }

  def setCollapsed(collapsed: Boolean) {
    element.setVisible(!collapsed)
    if (collapsed) {
      remove(open)
      add(closed, java.awt.BorderLayout.NORTH)
    } else {
      remove(closed)
      add(open, java.awt.BorderLayout.NORTH)
    }
    parent.pack()
  }

  def isCollapsed = element.isVisible

}
