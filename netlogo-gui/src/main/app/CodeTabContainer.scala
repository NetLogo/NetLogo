// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ BorderLayout, Dimension, Frame, GraphicsEnvironment, Point, Window }
import javax.swing.{ JFrame, JTabbedPane, WindowConstants }
import org.nlogo.window.Event.LinkChild

object CodeTabContainer {
  def findWindowLocation(parent: Window, child: Window, horizontalSpacing: Int): Point = {
    val availBounds = GraphicsEnvironment.getLocalGraphicsEnvironment.getMaximumWindowBounds
    val parentLocation = parent.getLocationOnScreen
    val hasRoomToRight = parent.getLocation.x + parent.getWidth + horizontalSpacing + child.getWidth <= availBounds.x + availBounds.width
    val hasRoomToLeft = availBounds.x  <= parent.getLocation.x - horizontalSpacing - child.getWidth
    val yLoc = parentLocation.y + parent.getInsets.top
    // Detached code tab location priority list:
    //   1) left edge to the right of the parent
    //   2) right edge to the left of the parent
    //   3) right edge to the left of the screen
    if (hasRoomToRight) {
      return new Point(parentLocation.x + parent.getWidth() + horizontalSpacing, yLoc)
    } else if (hasRoomToLeft) {
      return new Point(parent.getLocation.x - child.getWidth - horizontalSpacing, yLoc)
    } else {
      return new Point(availBounds.x + availBounds.width - child.getWidth - horizontalSpacing, yLoc)
    }
  }
}

// This is the separate code tab window.
// It contains the CodeTabsPanel which owns and manages the CodeTabs.
// It is created and destroyed 'on demand' as needed. AAB 10/2020
// parent              CodeTabContainer's LinkParent (a Frame).
// codeTabbedPane      CodeTabContainer's JTabbedPane
// codeTabDimension    CodeTabContainer's Dimension (gives size)
// horizontalSpacing:  Horizontal spacing between CodeTabContainer and parent
class CodeTabContainer(parent:            Frame,
                       codeTabbedPane:    JTabbedPane,
                       codeTabDimension:  Dimension = new Dimension(600, 400),
                       horizontalSpacing: Int = 10) extends JFrame with LinkChild {

  this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)
  this.add(codeTabbedPane, BorderLayout.CENTER)
  this.setSize(codeTabDimension)

  this.setLocation(CodeTabContainer.findWindowLocation(parent, this, horizontalSpacing))

  this.setVisible(true)
  // This is needed for proper Event handling AAB 10/2020
  def getLinkParent: Frame = { parent }
}
