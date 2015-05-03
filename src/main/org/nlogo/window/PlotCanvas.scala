// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Cursor, Graphics, Point }
import java.awt.event.{ MouseAdapter, MouseEvent, MouseMotionListener }
import javax.swing.JPanel
import org.nlogo.api.Approximate
import org.nlogo.plot.{ Plot, PlotPainter }

class PlotCanvas(private var plot: Plot) extends JPanel {
  private var painter = new PlotPainter(plot)
  setOpaque(true)
  setBackground(Color.WHITE)
  setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR))
  addMouseListener(_mouseListener)
  addMouseMotionListener(_mouseMotionListener)

  def setPlot(_plot: Plot) =  {
    plot = _plot
    painter = new PlotPainter(plot)
  }

  def repaintIfNeeded() = {
    painter.setupOffscreenImage(getWidth, getHeight)
    painter.refresh()
    repaint()
  }

  /// mouse handling

  private var mouseHere = false
  private var mouseLoc: Point = null

  private val _mouseListener = new MouseAdapter {
      override def mouseEntered(e: MouseEvent) = {
        mouseHere = true
        mouseLoc = e.getPoint
        repaint()
      }
      override def mouseExited(e: MouseEvent) = {
        mouseHere = false
        repaint()
      }
    }
  private val _mouseMotionListener = new MouseMotionListener {
      def mouseDragged(e: MouseEvent) = {
        mouseLoc = e.getPoint
        mouseHere = mouseLoc.x >= 0 && mouseLoc.y >= 0 &&
                mouseLoc.x < getBounds().width && mouseLoc.y < getBounds().height
        repaint()
      }
      def mouseMoved(e: MouseEvent) = {
        mouseHere = true  // shouldn't be necessary, but Linux and OS X VM's are flaky
        // (note: comment may out of date for Java 1.4 - ST 8/9/03)
        mouseLoc = e.getPoint
        repaint()
      }
    }

  /// other methods

  override def paintComponent(g: Graphics) = {
    painter.setupOffscreenImage(getWidth, getHeight)
    painter.drawImage(g)
    if(mouseHere) drawMouseCoords(g)
  }

  private def drawMouseCoords(g: Graphics) = {
    val fontMetrics = getFontMetrics(getFont)
    val fontHeight = fontMetrics.getAscent

    // prepare xcor
    val xPrecision =
      -StrictMath.floor(StrictMath.log(fromScreenXCor(1) - fromScreenXCor(0)) / StrictMath.log(10)).toInt
    var xcor = Approximate.approximate(fromScreenXCor(mouseLoc.x), xPrecision).toString
    if(xPrecision <= 0)
      // strip the trailing ".0"
      xcor = xcor.substring(0, xcor.length() - 2)
    val xcorWidth = fontMetrics.stringWidth(xcor)

    // prepare ycor
    val yPrecision =
      -StrictMath.floor(StrictMath.log(fromScreenYCor(0) - fromScreenYCor(1)) / StrictMath.log(10)).toInt
    var ycor = Approximate.approximate(fromScreenYCor(mouseLoc.y), yPrecision).toString
    if (yPrecision <= 0)
      // strip the trailing ".0"
      ycor = ycor.substring(0, ycor.length() - 2)
    val ycorWidth = fontMetrics.stringWidth(ycor)

    // position & draw xcor
    var xx = mouseLoc.x - xcorWidth / 2
    xx = StrictMath.max(xx, ycorWidth)
    xx = StrictMath.min(xx, getBounds().width - xcorWidth)
    val xy = getBounds().height - fontHeight + 1
    g.setColor(InterfaceColors.PLOT_BACKGROUND)
    g.fillRect(xx - 1, xy, xcorWidth + 1, fontHeight + 1)
    g.setColor(Color.BLACK)
    g.drawString(xcor, xx, xy + fontHeight - 1)

    // position & draw ycor
    var yy = mouseLoc.y - fontHeight / 2
    yy = StrictMath.max(yy, 0)
    yy = StrictMath.min(yy, getBounds().height - 2 * fontHeight)
    val yx = 0
    g.setColor(InterfaceColors.PLOT_BACKGROUND)
    g.fillRect(yx, yy, ycorWidth + 1, fontHeight + 1)
    g.setColor(Color.BLACK)
    g.drawString(ycor, yx, yy + fontHeight - 1)
  }

  private def fromScreenXCor(screenX: Double) = {
    val width = getWidth - 1
    val range = plot.state.xMax - plot.state.xMin
    plot.state.xMin + screenX * range / width
  }

  private def fromScreenYCor(screenY: Double) = {
    val height = getHeight - 1
    val range = plot.state.yMax - plot.state.yMin
    plot.state.yMax - screenY * range / height
  }
}
