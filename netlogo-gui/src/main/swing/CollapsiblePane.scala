// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ BorderLayout, FlowLayout }
import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.{ JComponent, JDialog, JLabel, JPanel }

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class CollapsiblePane(title: String, element: JComponent, parent: JDialog)
  extends JPanel(new BorderLayout) with ThemeSync {

  private val titleLabel = new JLabel(title)
  private val arrow = new CollapsibleArrow

  arrow.setOpen(element.isVisible)

  titleLabel.setIcon(arrow)

  private val titlePanel = new JPanel(new FlowLayout(FlowLayout.LEADING))

  titlePanel.add(titleLabel)

  locally {
    val mouseListener = new MouseAdapter {
      override def mouseClicked(e: MouseEvent) {
        setOpen(!isOpen)
      }
    }

    titlePanel.addMouseListener(mouseListener)
    titleLabel.addMouseListener(mouseListener)
  }

  add(titlePanel, BorderLayout.NORTH)
  add(element, BorderLayout.CENTER)

  def setOpen(open: Boolean): Unit = {
    element.setVisible(open)
    arrow.setOpen(open)
    parent.pack()
    repaint()
  }

  def isOpen = element.isVisible

  override def syncTheme(): Unit = {
    titlePanel.setBackground(InterfaceColors.dialogBackground)
    titleLabel.setForeground(InterfaceColors.dialogText)

    element match {
      case ts: ThemeSync => ts.syncTheme()
      case _ =>
    }
  }
}
