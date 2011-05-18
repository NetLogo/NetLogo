package org.nlogo.swing

import java.awt.{ Dimension, Point }
import javax.swing.JWindow
import java.awt.event.{ MouseAdapter, MouseEvent }
import org.nlogo.awt.Utils.convertPointToScreen

class WindowResizer(window: JWindow)
extends javax.swing.JPanel {

  private var mousePressAbsLoc: Point = null
  private var sizeWhenPressed: Dimension = null

  private val adapter = new MouseAdapter {
    override def mousePressed(e: MouseEvent) {
      val mousePressLoc = e.getPoint
      mousePressAbsLoc = new java.awt.Point(mousePressLoc)
      convertPointToScreen(mousePressAbsLoc, WindowResizer.this)
      sizeWhenPressed = window.getSize
    }
    override def mouseDragged(e: MouseEvent) {
      val dragAbsLoc = new java.awt.Point(e.getPoint)
      convertPointToScreen(dragAbsLoc, WindowResizer.this)
      window.setSize(
        sizeWhenPressed.width + (dragAbsLoc.x - mousePressAbsLoc.x),
        sizeWhenPressed.height + (dragAbsLoc.y - mousePressAbsLoc.y))
    }
  }

  addMouseListener(adapter)
  addMouseMotionListener(adapter)

}
