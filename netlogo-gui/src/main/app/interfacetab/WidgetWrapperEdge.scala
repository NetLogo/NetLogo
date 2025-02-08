// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{ Color, Graphics, Rectangle }
import javax.swing.JPanel

import org.nlogo.awt.Colors

object WidgetWrapperEdge {
  abstract class Type

  case object TOP extends Type
  case object BOTTOM extends Type
  case object SIDE extends Type
}

// not JComponent otherwise paintComponent() doesn't paint the
// background color for reasons I can't fathom - ST 8/9/03
class WidgetWrapperEdge(tpe: WidgetWrapperEdge.Type, eastBorder: Int, westBorder: Int, var handles: Boolean) extends JPanel {
  private var cornerHandles = true

  setBackground(Color.GRAY)
  setOpaque(true)

  def handles(show: Boolean): Unit = {
    handles = show
  }

  def cornerHandles(show: Boolean): Unit = {
    cornerHandles = show
  }

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)

    g.setColor(getBackground)

    tpe match {
      case WidgetWrapperEdge.TOP => paintTop(g)
      case WidgetWrapperEdge.BOTTOM => paintBottom(g)
      case WidgetWrapperEdge.SIDE => paintSide(g)
      case _ => throw new IllegalStateException("type = " + tpe);
    }
  }

  private def paintTop(g: Graphics): Unit = {
    if (getWidth == 0 || getHeight == 0)
      return

    val bleed = 5

    if (westBorder == 0) // draw 3D border on top and right edges only
      drawConvexRect(g, new Rectangle(-bleed, getY, getWidth + bleed, getHeight + bleed))
    else // draw it on the left, too
      drawConvexRect(g, new Rectangle(0, getY, getWidth, getHeight + bleed))

    // now draw 3D border on bottom edge
    // ...westBorder controls whether it extends all the way to our left edge or not
    val oldClip = g.getClip

    g.setClip(westBorder - 2, getHeight - 3, getWidth - eastBorder - westBorder + 3, 3)
    drawConvexRect(g, new Rectangle(-5, 0, getWidth + 10, getHeight))
    g.setClip(oldClip)

    // now draw handles
    if (handles) {
      g.setColor(Color.BLACK)

      if (cornerHandles)
        g.fillRect(0, 0, westBorder, getHeight)

      g.fillRect(westBorder + (getWidth - eastBorder - westBorder - WidgetWrapper.HANDLE_WIDTH) / 2, 0,
                 WidgetWrapper.HANDLE_WIDTH, getHeight)

      if (cornerHandles)
        g.fillRect(getWidth - eastBorder, 0, eastBorder, getHeight)
    }
  }

  private def paintBottom(g: Graphics): Unit = {
    if (getWidth == 0 || getHeight == 0)
      return

    val oldClip = g.getClip

    drawConvexRect(g, new Rectangle(0, -5, getWidth, getHeight + 5))

    g.setClip(eastBorder - 1, 0, getWidth - eastBorder - westBorder + 2, 3)
    drawConvexRect(g, new Rectangle(0, 0, getWidth, getHeight))
    g.setClip(oldClip)

    // now draw handles
    if (handles) {
      g.setColor(Color.BLACK)

      if (cornerHandles)
        g.fillRect(0, 0, westBorder, getHeight)

      g.fillRect(westBorder + (getWidth - eastBorder - westBorder - WidgetWrapper.HANDLE_WIDTH) / 2, 0,
                 WidgetWrapper.HANDLE_WIDTH, getHeight)

      if (cornerHandles)
        g.fillRect(getWidth - eastBorder, 0, eastBorder, getHeight)
    }
  }

  private def paintSide(g: Graphics): Unit = {
    if (getWidth == 0 || getHeight == 0)
      return

    drawConvexRect(g, new Rectangle(0, -5, getWidth, getHeight + 10))

    // now draw handles
    if (handles) {
      g.setColor(Color.BLACK)
      g.fillRect(0, (getHeight - WidgetWrapper.HANDLE_WIDTH) / 2, getWidth, WidgetWrapper.HANDLE_WIDTH)
    }
  }

  private def drawConvexRect(g: Graphics, r: Rectangle): Unit = {
    g.setColor(Colors.mixColors(getForeground, getBackground, 0.5))
    g.drawRect(r.x, r.y, r.width, r.height)
  }
}
