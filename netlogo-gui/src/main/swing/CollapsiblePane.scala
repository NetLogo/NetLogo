// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ BorderLayout, FlowLayout }
import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.{ JComponent, JDialog, JLabel, JPanel }

import org.nlogo.swing.Utils.icon

object CollapsiblePane {
  private val OpenIcon   = icon("/images/popup.gif")
  private val ClosedIcon = icon("/images/closedarrow.gif")
}

class CollapsiblePane(title: String, element: JComponent, parent: JDialog)
extends JPanel(new BorderLayout) {
  import CollapsiblePane._

  private val titleLabel = new JLabel(title)

  locally {
    titleLabel.setIcon(OpenIcon)

    val titlePanel = new JPanel(new FlowLayout(FlowLayout.LEADING))
    titlePanel.add(titleLabel)
    titlePanel.addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent) = setOpen(!isOpen)
    })

    add(titlePanel, BorderLayout.NORTH)
    add(element,    BorderLayout.CENTER)
  }

  def setOpen(open: Boolean): Unit = {
    element.setVisible(open)
    titleLabel.setIcon(if (open) OpenIcon else ClosedIcon)
    parent.pack()
  }

  def isOpen = element.isVisible
}
