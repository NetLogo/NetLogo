// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.event.{ActionEvent, ActionListener, MouseEvent, MouseAdapter}
import javax.swing.{JPanel, ImageIcon, JPopupMenu, JMenuItem}
import java.awt.{RenderingHints, Graphics2D, Graphics, Dimension, Color}

class ToolBarComboBox(val items: Array[JMenuItem]) extends JPanel {

  private var selected = items(0)

  locally {
    setBorder(Utils.createWidgetBorder())
    if (System.getProperty("os.name").startsWith("Mac")) setBackground(Color.WHITE)
    addMouseListener(new MouseAdapter() {
      override def mousePressed(e: MouseEvent) {
        if (isEnabled()) {
          val menu = new WrappingPopupMenu()
          populate(menu)
          menu.show(ToolBarComboBox.this, 0, getHeight)
        }
      }
    })
    org.nlogo.awt.Fonts.adjustDefaultFont(this)
  }

  def populate(menu: JPopupMenu) {
    for (item <- items) {
      menu.add(item)
      item.addActionListener(new ActionListener() {
        def actionPerformed(e: ActionEvent) {
          selected = item
          ToolBarComboBox.this.repaint()
        }
      })
    }
  }

  def getSelectedItem:JMenuItem = selected
  def setSelectedString(toSelect:String) {
    selected = items.find(_.getText == toSelect).getOrElse(selected)
    repaint()
  }

  // handle the case where the item that was selected has now become disabled
  def updateSelected() {
    if(!selected.isEnabled)
      for(newGuy <- items.find(_.isEnabled)) {
        selected = newGuy
        repaint()
      }
  }

  override def getMinimumSize = new Dimension(11,20)
  override def getPreferredSize = {
    val xpad = 5
    val fontMetrics = getFontMetrics(getFont)
    val size = new Dimension(11, 20)
    for (item <- items) {
      val w = fontMetrics.stringWidth(item.getText) + 2 * xpad + 11 + item.getIcon.getIconWidth
      if (w > size.width) size.width = w
    }
    size
  }

  override def paintComponent(g:Graphics) {
    val g2d = g.asInstanceOf[Graphics2D]
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    super.paintComponent(g)
    // Draw Label
    g.setColor(if (isEnabled) getForeground else Color.GRAY)
    val fontMetrics = g.getFontMetrics
    val icon = selected.getIcon.asInstanceOf[ImageIcon]
    if(isEnabled) g.drawImage(icon.getImage, 2, 1, icon.getImageObserver)
    g.drawString(selected.getText, 5 + icon.getIconWidth, fontMetrics.getMaxAscent + 2)
    // Draw Arrow
    val xpnts = Array(getWidth - 13, getWidth - 9, getWidth - 5)
    val ypnts = Array( (getHeight / 2) - 2, (getHeight / 2) + 2, (getHeight / 2) - 2 )
    g.fillPolygon(xpnts, ypnts, 3)
  }
}
