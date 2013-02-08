// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Dimension, Point }
import javax.swing.JWindow
import java.awt.event.{ MouseAdapter, MouseMotionAdapter, MouseEvent }
import org.nlogo.awt.Coordinates.convertPointToScreen

// In Java 6 you can just use a single MouseAdapter for everything, but for Java 5 you need a
// separate MouseMotionAdapter. - ST 5/27/11

class WindowResizer(window: JWindow)
extends javax.swing.JPanel {

  private var mousePressAbsLoc: Point = null
  private var sizeWhenPressed: Dimension = null

  private val adapter = new MouseAdapter {
    override def mousePressed(e: MouseEvent) {
      val mousePressLoc = e.getPoint
      mousePressAbsLoc = convertPointToScreen(mousePressLoc, WindowResizer.this)
      sizeWhenPressed = window.getSize
    }
  }
  private val motionAdapter = new MouseMotionAdapter {
    override def mouseDragged(e: MouseEvent) {
      val dragAbsLoc =
        convertPointToScreen(e.getPoint, WindowResizer.this)
      window.setSize(
        sizeWhenPressed.width + (dragAbsLoc.x - mousePressAbsLoc.x),
        sizeWhenPressed.height + (dragAbsLoc.y - mousePressAbsLoc.y))
    }
  }

  addMouseListener(adapter)
  addMouseMotionListener(motionAdapter)

}
