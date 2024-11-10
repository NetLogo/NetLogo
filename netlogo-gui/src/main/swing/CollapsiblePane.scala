// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ BorderLayout, Component, FlowLayout, Graphics }
import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.{ Icon, JComponent, JDialog, JLabel, JPanel }

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class CollapsiblePane(title: String, element: JComponent, parent: JDialog)
  extends JPanel(new BorderLayout) with ThemeSync {

  private class Arrow extends Icon {
    def getIconWidth = 9
    def getIconHeight = 9

    def paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(InterfaceColors.DIALOG_TEXT)

      if (isOpen) {
        g2d.drawLine(x, y + 2, x + 4, y + 6)
        g2d.drawLine(x + 4, y + 6, x + 8, y + 2)
      }

      else {
        g2d.drawLine(x + 2, y + 8, x + 6, y + 4)
        g2d.drawLine(x + 6, y + 4, x + 2, y)
      }
    }
  }
  
  private val titleLabel = new JLabel(title)

  titleLabel.setIcon(new Arrow)

  private val titlePanel = new JPanel(new FlowLayout(FlowLayout.LEADING))

  titlePanel.add(titleLabel)
  titlePanel.addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent) = setOpen(!isOpen)
  })

  add(titlePanel, BorderLayout.NORTH)
  add(element, BorderLayout.CENTER)

  def setOpen(open: Boolean): Unit = {
    element.setVisible(open)
    parent.pack()
    repaint()
  }

  def isOpen = element.isVisible

  def syncTheme() {
    titlePanel.setBackground(InterfaceColors.DIALOG_BACKGROUND)
    titleLabel.setForeground(InterfaceColors.DIALOG_TEXT)

    element match {
      case ts: ThemeSync => ts.syncTheme()
      case _ =>
    }
  }
}
