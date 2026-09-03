// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.BorderLayout
import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.{ JComponent, JDialog, JLabel, JPanel }

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class CollapsiblePane(title: String, element: JComponent, parent: JDialog)
  extends JPanel(new BorderLayout) with ThemeSync {

  private val titleLabel = new JLabel(title) with Zoomable {
    override def getIconTextGap: Int =
      Utils.zoom(super.getIconTextGap)
  }

  private val arrow = new CollapsibleArrow(element.isVisible)

  titleLabel.setIcon(arrow)

  locally {
    val listener = new MouseAdapter {
      override def mouseClicked(e: MouseEvent): Unit = {
        setOpen(!isOpen)
      }
    }

    add(new BoxRow(titleLabel, BoxAlign.Start) {
      setBorder(new ZoomableBorder(6, 6, 6, 6))

      addMouseListener(listener)
    }, BorderLayout.NORTH)

    titleLabel.addMouseListener(listener)
  }

  add(element, BorderLayout.CENTER)

  def setOpen(open: Boolean): Unit = {
    if (element.isVisible != open) {
      element.setVisible(open)
      arrow.setOpen(open)
      parent.pack()
      repaint()
    }
  }

  def isOpen = element.isVisible

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.dialogBackground())

    titleLabel.setForeground(InterfaceColors.dialogText())

    element match {
      case ts: ThemeSync => ts.syncTheme()
      case _ =>
    }
  }
}
