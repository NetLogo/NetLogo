// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{ Color, Component, Cursor, Graphics, Point }
import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent, MouseMotionAdapter }
import javax.swing.{ AbstractAction, Action, JButton, JLayeredPane, JPanel, JSplitPane }

import org.nlogo.core.I18N
import org.nlogo.swing.{ Transparent, Utils }
import org.nlogo.theme.InterfaceColors

private class SizeButton(expand: Boolean, splitPane: SplitPane) extends JButton with Transparent {
  setBorder(null)

  if (expand) {
    setAction(new AbstractAction {
      def actionPerformed(e: ActionEvent) {
        if (splitPane.getDividerLocation >= splitPane.maxDividerLocation) {
          splitPane.resetToPreferredSizes()
        }

        else if (splitPane.getDividerLocation > 0) {
          splitPane.setDividerLocation(0)
        }
      }
    })
  }

  else {
    setAction(new AbstractAction {
      def actionPerformed(e: ActionEvent) {
        if (splitPane.getDividerLocation <= 0) {
          splitPane.resetToPreferredSizes()
        }

        else if (splitPane.getDividerLocation < splitPane.maxDividerLocation) {
          splitPane.setDividerLocation(splitPane.maxDividerLocation)
        }
      }
    })
  }

  override def paintComponent(g: Graphics) {
    super.paintComponent(g)

    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(Color.BLACK)

    splitPane.getOrientation match {
      case JSplitPane.HORIZONTAL_SPLIT =>
        if (expand)
          g2d.fillPolygon(Array(getWidth / 2, getWidth / 2 + 5, getWidth / 2 - 5),
                          Array(getHeight / 2 - 2, getHeight / 2 + 2, getHeight / 2 + 2), 3)
        else
          g2d.fillPolygon(Array(getWidth / 2, getWidth / 2 + 5, getWidth / 2 - 5),
                          Array(getHeight / 2 + 2, getHeight / 2 - 2, getHeight / 2 - 2), 3)
      case JSplitPane.VERTICAL_SPLIT =>
        if (expand)
          g2d.fillPolygon(Array(getWidth / 2 - 2, getWidth / 2 + 2, getWidth / 2 + 2),
                          Array(getHeight / 2, getHeight / 2 - 5, getHeight / 2 + 5), 3)
        else
          g2d.fillPolygon(Array(getWidth / 2 + 2, getWidth / 2 - 2, getWidth / 2 - 2),
                          Array(getHeight / 2, getHeight / 2 - 5, getHeight / 2 + 5), 3)
    }
  }
}

private class SplitPaneDivider(splitPane: SplitPane) extends JPanel(null) {
  private val expandButton = new SizeButton(true, splitPane)
  private val contractButton = new SizeButton(false, splitPane)

  add(expandButton)
  add(contractButton)
  
  setBackground(InterfaceColors.SPLIT_PANE_DIVIDER_BACKGROUND)

  private val dragRadius = 3

  private var offset = new Point(0, 0)

  addMouseListener(new MouseAdapter {
    override def mouseEntered(e: MouseEvent) {
      splitPane.getOrientation match {
        case JSplitPane.HORIZONTAL_SPLIT => setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR))
        case JSplitPane.VERTICAL_SPLIT => setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR))
      }
    }

    override def mouseExited(e: MouseEvent) {
      setCursor(Cursor.getDefaultCursor)
    }

    override def mousePressed(e: MouseEvent) {
      offset = e.getPoint
    }
  })

  addMouseMotionListener(new MouseMotionAdapter {
    override def mouseDragged(e: MouseEvent) {
      e.translatePoint(getX, getY)

      splitPane.getOrientation match {
        case JSplitPane.HORIZONTAL_SPLIT => splitPane.setDividerLocation(e.getY - offset.y)
        case JSplitPane.VERTICAL_SPLIT => splitPane.setDividerLocation(e.getX - offset.x)
      }
    }
  })

  override def doLayout() {
    val size = splitPane.getDividerSize

    splitPane.getOrientation match {
      case JSplitPane.HORIZONTAL_SPLIT =>
        expandButton.setBounds(0, 0, size, size)
        contractButton.setBounds(size, 0, size, size)
      case JSplitPane.VERTICAL_SPLIT =>
        expandButton.setBounds(0, 0, size, size)
        contractButton.setBounds(0, size, size, size)
    }
  }

  override def paintComponent(g: Graphics) {
    super.paintComponent(g)

    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(Color.WHITE)
    g2d.fillOval(getWidth / 2 - dragRadius, getHeight / 2 - dragRadius, dragRadius * 2, dragRadius * 2)
  }
}

private class SplitPane(mainComponent: Component, topComponent: Component, commandCenterToggleAction: Action)
  extends JLayeredPane {

  private val divider = new SplitPaneDivider(this)

  add(mainComponent, JLayeredPane.DEFAULT_LAYER)
  add(topComponent, JLayeredPane.PALETTE_LAYER)
  add(divider, JLayeredPane.PALETTE_LAYER)

  private var orientation = JSplitPane.HORIZONTAL_SPLIT
  private var dividerLocation = 0
  private val dividerSize = 18

  def getOrientation: Int = orientation

  def setOrientation(orientation: Int) {
    this.orientation = orientation

    revalidate()
    dividerChanged()
  }

  def getDividerLocation: Int = dividerLocation

  def setDividerLocation(location: Int) {
    dividerLocation = location.max(0).min(maxDividerLocation)

    revalidate()
    dividerChanged()
  }

  private def dividerChanged() {
    commandCenterToggleAction.putValue(Action.NAME,
      if (dividerLocation < maxDividerLocation) I18N.gui.get("menu.tools.hideCommandCenter")
      else I18N.gui.get("menu.tools.showCommandCenter"))
  }

  def getDividerSize: Int = dividerSize

  def resetToPreferredSizes() {
    orientation match {
      case JSplitPane.HORIZONTAL_SPLIT =>
        setDividerLocation(getHeight - topComponent.getPreferredSize.height - dividerSize)
      case JSplitPane.VERTICAL_SPLIT =>
        setDividerLocation(getWidth - topComponent.getPreferredSize.width - dividerSize)
    }
  }

  def maxDividerLocation: Int = {
    orientation match {
      case JSplitPane.HORIZONTAL_SPLIT => getHeight - dividerSize
      case JSplitPane.VERTICAL_SPLIT => getWidth - dividerSize
    }
  }

  override def doLayout() {
    orientation match {
      case JSplitPane.HORIZONTAL_SPLIT =>
        mainComponent.setBounds(0, 0, getWidth, dividerLocation)
      case JSplitPane.VERTICAL_SPLIT =>
        mainComponent.setBounds(0, 0, dividerLocation, getHeight)
    }

    if (dividerLocation > maxDividerLocation)
      dividerLocation = maxDividerLocation
    
    orientation match {
      case JSplitPane.HORIZONTAL_SPLIT =>
        topComponent.setBounds(0, dividerLocation + dividerSize, getWidth, getHeight - dividerLocation - dividerSize)
        divider.setBounds(0, dividerLocation, getWidth, dividerSize)
      case JSplitPane.VERTICAL_SPLIT =>
        topComponent.setBounds(dividerLocation + dividerSize, 0, getWidth - dividerLocation - dividerSize, getHeight)
        divider.setBounds(dividerLocation, 0, dividerSize, getHeight)
    }

    dividerChanged()
  }

  override def setBounds(x: Int, y: Int, width: Int, height: Int) {
    if (dividerLocation > 0 && dividerLocation < maxDividerLocation) {
      orientation match {
        case JSplitPane.HORIZONTAL_SPLIT =>
          dividerLocation = 1.max(dividerLocation + (height - getHeight))
        case JSplitPane.VERTICAL_SPLIT =>
          dividerLocation = 1.max(dividerLocation + (width - getWidth))
      }
    }

    else if (dividerLocation < 0)
      dividerLocation = 0

    super.setBounds(x, y, width, height)
  }
}
