// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.swing
import java.awt.event.{ActionEvent, MouseAdapter, MouseEvent}
import java.awt.{Dimension, Graphics, Graphics2D}
import javax.swing.{AbstractAction, JButton, JPopupMenu}

abstract class ToolBarMenu(name: String) extends JButton(name) {
  setMinimumSize(new Dimension(11,20))
  if (System.getProperty("os.name").startsWith("Mac")) setBackground(java.awt.Color.WHITE)
  setAction(new AbstractAction(name) {
    override def actionPerformed(e: ActionEvent): Unit = popup()
  })
  // This is so a user may treat this like a menu drop down:
  // clicking and holding, dragging to the item of their choice, and releasing.
  addMouseListener(new MouseAdapter() {
    override def mousePressed(e: MouseEvent): Unit = doClick()
  })
  org.nlogo.awt.Fonts.adjustDefaultFont(this)

  def popup(): Unit = {
    val menu = new WrappingPopupMenu
    populate(menu)
    menu.setVisible(false)
    menu.show(this, 0, getHeight)
    menu.pack()
    menu.setVisible(true)
  }

  protected def populate(menu: JPopupMenu): Unit

  override def getPreferredSize: Dimension = {
    val size = getMinimumSize
    val xpad = 5
    val ypad = 2
    val fontMetrics = getFontMetrics(getFont)
    size.width = StrictMath.max(size.width, fontMetrics.stringWidth(name) + 2 * xpad + 11)
    size.height = StrictMath.max(size.height, fontMetrics.getMaxDescent + fontMetrics.getMaxAscent + 2 * ypad)
    size
  }

  override def paintComponent(g: Graphics): Unit = {
    val g2d = g.asInstanceOf[Graphics2D]
    g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
    g.setColor(getBackground)
    g.fillRect(0, 0, getWidth, getHeight)
    // Draw Label
    g.setColor(getForeground)
    val fontMetrics = g.getFontMetrics
    g.drawString(name, 5, fontMetrics.getMaxAscent + 2)
    // Draw Arrow
    val xpnts = Array[Int](getWidth - 13, getWidth - 9, getWidth - 5)
    val ypnts = Array[Int]((getHeight / 2) - 2, (getHeight / 2) + 2, (getHeight / 2) - 2)
    g.fillPolygon(xpnts, ypnts, 3)
  }
}
