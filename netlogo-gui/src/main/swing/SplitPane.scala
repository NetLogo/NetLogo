// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Color, Component, Cursor, Dimension, Graphics, Point }
import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent, MouseMotionAdapter }
import javax.swing.{ AbstractAction, Action, JButton, JLayeredPane, JPanel, JSplitPane }

import org.nlogo.core.I18N
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

private class SizeButton(expand: Boolean, splitPane: SplitPane) extends JButton with Transparent {
  setBorder(null)

  if (expand) {
    setAction(new AbstractAction {
      def actionPerformed(e: ActionEvent): Unit = {
        if (splitPane.getDividerLocation >= splitPane.maxClosedDividerLocation) {
          splitPane.resetToLastOpenSizes()
        } else if (splitPane.getDividerLocation > 0) {
          splitPane.setDividerLocation(0)
        }
      }
    })
  }

  else {
    setAction(new AbstractAction {
      def actionPerformed(e: ActionEvent): Unit = {
        if (splitPane.getDividerLocation <= 0) {
          splitPane.resetToLastOpenSizes()
        } else if (splitPane.getDividerLocation < splitPane.maxClosedDividerLocation) {
          splitPane.setDividerLocation(splitPane.maxClosedDividerLocation)
        }
      }
    })
  }

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)

    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(Color.BLACK)

    splitPane.getOrientation match {
      case JSplitPane.HORIZONTAL_SPLIT =>
        if (expand) {
          g2d.fillPolygon(Array(getWidth / 2, getWidth / 2 + 5, getWidth / 2 - 5),
                          Array(getHeight / 2 - 2, getHeight / 2 + 2, getHeight / 2 + 2), 3)
        } else {
          g2d.fillPolygon(Array(getWidth / 2, getWidth / 2 + 5, getWidth / 2 - 5),
                          Array(getHeight / 2 + 2, getHeight / 2 - 2, getHeight / 2 - 2), 3)
        }

      case JSplitPane.VERTICAL_SPLIT =>
        if (expand) {
          g2d.fillPolygon(Array(getWidth / 2 - 2, getWidth / 2 + 2, getWidth / 2 + 2),
                          Array(getHeight / 2, getHeight / 2 - 5, getHeight / 2 + 5), 3)
        } else {
          g2d.fillPolygon(Array(getWidth / 2 + 2, getWidth / 2 - 2, getWidth / 2 - 2),
                          Array(getHeight / 2, getHeight / 2 - 5, getHeight / 2 + 5), 3)
        }
    }
  }
}

private class SplitPaneDivider(splitPane: SplitPane) extends JPanel(null) with ThemeSync {
  private val expandButton = new SizeButton(true, splitPane)
  private val contractButton = new SizeButton(false, splitPane)

  add(expandButton)
  add(contractButton)

  private val dragRadius = 3

  private var offset = new Point(0, 0)

  addMouseListener(new MouseAdapter {
    override def mouseEntered(e: MouseEvent): Unit = {
      splitPane.getOrientation match {
        case JSplitPane.HORIZONTAL_SPLIT => setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR))
        case JSplitPane.VERTICAL_SPLIT => setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR))
      }
    }

    override def mouseExited(e: MouseEvent): Unit = {
      setCursor(Cursor.getDefaultCursor)
    }

    override def mousePressed(e: MouseEvent): Unit = {
      offset = e.getPoint
    }
  })

  addMouseMotionListener(new MouseMotionAdapter {
    override def mouseDragged(e: MouseEvent): Unit = {
      e.translatePoint(getX, getY)

      splitPane.getOrientation match {
        case JSplitPane.HORIZONTAL_SPLIT =>
          splitPane.dragDividerLocation(e.getY - offset.y)
        case JSplitPane.VERTICAL_SPLIT =>
          splitPane.dragDividerLocation(e.getX - offset.x)
      }
    }
  })

  override def doLayout(): Unit = {
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

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)

    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(Color.WHITE)
    g2d.fillOval(getWidth / 2 - dragRadius, getHeight / 2 - dragRadius, dragRadius * 2, dragRadius * 2)
  }

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.splitPaneDividerBackground())
  }
}

class SplitPane(mainComponent: Component, topComponent: Component, commandCenterToggleAction: Option[Action])
  extends JLayeredPane with ThemeSync {

  private val divider = new SplitPaneDivider(this)

  add(mainComponent, JLayeredPane.DEFAULT_LAYER)
  add(topComponent, JLayeredPane.PALETTE_LAYER)
  add(divider, JLayeredPane.PALETTE_LAYER)

  private var orientation = JSplitPane.HORIZONTAL_SPLIT
  private var dividerLocation = 0
  private var lastOpenDividerLocation = 0
  private val dividerSize = 18

  def getOrientation: Int = orientation

  def setOrientation(orientation: Int): Unit = {
    this.orientation = orientation

    revalidate()
    dividerChanged()
  }

  def getDividerLocation: Int = dividerLocation

  def setDividerLocation(location: Int): Unit = {
    dividerLocation = location.max(0).min(maxClosedDividerLocation)

    revalidate()
    dividerChanged()
  }

  def dragDividerLocation(location: Int): Unit = {
    lastOpenDividerLocation = location.max(minOpenDividerLocation).min(maxOpenDividerLocation)

    if (location < minOpenDividerLocation / 2) {
      dividerLocation = 0
    } else if (location > maxOpenDividerLocation + (maxClosedDividerLocation - maxOpenDividerLocation) / 2) {
      dividerLocation = maxClosedDividerLocation
    } else {
      dividerLocation = lastOpenDividerLocation
    }

    revalidate()
    dividerChanged()
  }

  private def dividerChanged(): Unit = {
    commandCenterToggleAction.foreach(_.putValue(Action.NAME,
      if (dividerLocation < maxClosedDividerLocation) I18N.gui.get("menu.tools.hideCommandCenter")
      else I18N.gui.get("menu.tools.showCommandCenter")))
  }

  def getDividerSize: Int = dividerSize

  def resetToPreferredSizes(): Unit = {
    setDividerLocation(maxOpenDividerLocation)

    lastOpenDividerLocation = dividerLocation
  }

  def resetToLastOpenSizes(): Unit = {
    setDividerLocation(lastOpenDividerLocation)
  }

  def minOpenDividerLocation: Int =
    25

  def maxOpenDividerLocation: Int = {
    orientation match {
      case JSplitPane.HORIZONTAL_SPLIT => getHeight - topComponent.getPreferredSize.height - dividerSize
      case JSplitPane.VERTICAL_SPLIT => getWidth - topComponent.getPreferredSize.width - dividerSize
    }
  }

  def maxClosedDividerLocation: Int = {
    orientation match {
      case JSplitPane.HORIZONTAL_SPLIT => getHeight - dividerSize
      case JSplitPane.VERTICAL_SPLIT => getWidth - dividerSize
    }
  }

  override def doLayout(): Unit = {
    orientation match {
      case JSplitPane.HORIZONTAL_SPLIT =>
        mainComponent.setBounds(0, 0, getWidth, dividerLocation)
      case JSplitPane.VERTICAL_SPLIT =>
        mainComponent.setBounds(0, 0, dividerLocation, getHeight)
    }

    if (dividerLocation > maxClosedDividerLocation)
      dividerLocation = maxClosedDividerLocation

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

  override def setBounds(x: Int, y: Int, width: Int, height: Int): Unit = {
    if (dividerLocation > 0 && dividerLocation < maxClosedDividerLocation) {
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

  override def getPreferredSize: Dimension = {
    new Dimension(mainComponent.getPreferredSize.width,
                  mainComponent.getPreferredSize.height + topComponent.getPreferredSize.height + dividerSize)
  }

  override def syncTheme(): Unit = {
    divider.syncTheme()
  }
}
