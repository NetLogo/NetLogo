// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Dimension, Frame, GraphicsEnvironment, Point }
import javax.swing.{ JFrame, WindowConstants }

import org.nlogo.window.Event.LinkChild

class CodeTabsWindow(parent: Frame, tabs: TabsPanel) extends JFrame with LinkChild {
  val menuBar: MenuBar = new MenuBar(false)

  setJMenuBar(menuBar)

  setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)
  setSize(new Dimension(600, 400))
  setVisible(false)

  add(tabs)

  def getLinkParent = parent

  def open() {
    setLocation(findWindowLocation)
    setVisible(true)
  }

  private def findWindowLocation(): Point = {
    val horizontalSpacing = 10

    val availBounds = GraphicsEnvironment.getLocalGraphicsEnvironment.getMaximumWindowBounds
    val parentLocation = parent.getLocationOnScreen
    val hasRoomToRight = parent.getLocation.x + parent.getWidth + horizontalSpacing + getWidth <= availBounds.x + availBounds.width
    val hasRoomToLeft = availBounds.x  <= parent.getLocation.x - horizontalSpacing - getWidth
    // Detached code tab location priority list:
    //   1) left edge to the right of the parent
    //   2) right edge to the left of the parent
    //   3) right edge to the left of the screen
    val xLoc =
      if (hasRoomToRight)
        parentLocation.x + parent.getWidth() + horizontalSpacing
      else if (hasRoomToLeft)
        parent.getLocation.x - getWidth - horizontalSpacing
      else
        availBounds.x + availBounds.width - getWidth - horizontalSpacing
    new Point(xLoc, parentLocation.y + parent.getInsets.top)
  }
}
