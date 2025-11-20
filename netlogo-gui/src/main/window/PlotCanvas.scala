// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Cursor, Graphics, Point }
import java.awt.event.{ MouseAdapter, MouseEvent, MouseMotionListener }
import java.lang.{ Double => JDouble }
import javax.swing.JPanel

import org.nlogo.api.Approximate
import org.nlogo.plot.{ Plot, PlotPainter }
import org.nlogo.theme.InterfaceColors

class PlotCanvas(private val plot: Plot) extends JPanel {

  private val painter = new PlotPainter(plot)

  private var _isDirty                = true
  private var isMouseInside           = false
  private var mouseLoc: Option[Point] = None

  setOpaque(true)
  setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR))

  addMouseListener(
    new MouseAdapter() {
      override def mouseEntered(e: MouseEvent): Unit = {
        isMouseInside = true
        mouseLoc      = Option(e.getPoint())
        repaint()
      }
      override def mouseExited(e: MouseEvent): Unit = {
        isMouseInside = false
        repaint()
      }
    }
  )

  addMouseMotionListener(
    new MouseMotionListener() {

      override def mouseDragged(e: MouseEvent): Unit = {

        val point     = e.getPoint
        val isWithinX = point.x >= 0 && point.x < getBounds().width
        val isWithinY = point.y >= 0 && point.y < getBounds().height

        isMouseInside = isWithinX && isWithinY
        mouseLoc      = Option(point)

        repaint()

      }

      override def mouseMoved(e: MouseEvent): Unit = {
        isMouseInside = true
        mouseLoc      = Option(e.getPoint())
        repaint()
      }

    }
  )

  def repaintIfNeeded(): Unit = {
    if (isDirty) {
      painter.setupOffscreenImage(getWidth(), getHeight())
      painter.refresh()
      _isDirty = false
      repaint()
    }
  }

  def makeDirty(): Unit = {
    _isDirty = true
  }

  def isDirty = _isDirty

  /// other methods

  override def paintComponent(g: Graphics): Unit = {
    painter.setupOffscreenImage(getWidth(), getHeight())
    painter.drawImage(g)
    if (isMouseInside) {
      drawMouseCoords(g)
    }
  }

  private def drawMouseCoords(g: Graphics): Unit = {

    val (xcor, xcorWidth) = labelAndWidth( true)
    val (ycor, ycorWidth) = labelAndWidth(false)

    val (mouseX, mouseY) = mouseLoc.fold((0, 0)) { p => (p.x, p.y) }

    val fontHeight = getFontMetrics(getFont()).getAscent()

    val xx = {
      val midX     = mouseX - fontHeight / 2
      val boundedX = StrictMath.max(midX, ycorWidth)
      StrictMath.min(boundedX, getBounds().width - xcorWidth)
    }

    val xy = getBounds().height - fontHeight + 1

    g.setColor(InterfaceColors.plotMouseBackground())
    g.fillRect(xx - 1, xy, xcorWidth + 1, fontHeight + 1)
    g.setColor(InterfaceColors.plotMouseText())
    g.drawString(xcor, xx, xy + fontHeight - 1)

    // position & draw ycor
    val yy = {
      val midY     = mouseY - fontHeight / 2
      val boundedY = StrictMath.max(midY, 0)
      StrictMath.min(boundedY, getBounds().height - 2 * fontHeight)
    }

    val yx = 0

    g.setColor(InterfaceColors.plotMouseBackground())
    g.fillRect(yx, yy, ycorWidth + 1, fontHeight + 1)
    g.setColor(InterfaceColors.plotMouseText())
    g.drawString(ycor, yx, yy + fontHeight - 1)

  }

  def labelAndWidth(isX: Boolean): (String, Int) = {

    def fromScreenXCor(screenX: Double): Double = {
      val width = getWidth - 1
      val range = plot.xMax - plot.xMin
      plot.xMin + screenX * range / width
    }

    def fromScreenYCor(screenY: Double): Double = {
      val height = getHeight - 1
      val range  = plot.yMax - plot.yMin
      plot.yMax - screenY * range / height
    }

    val (mouseX, mouseY) = mouseLoc.fold((0, 0)) { p => (p.x, p.y) }

    val (firstCor, secondCor, mouseCor) =
      if (isX)
        (fromScreenXCor(1), fromScreenXCor(0), fromScreenXCor(mouseX))
      else
        (fromScreenYCor(0), fromScreenYCor(1), fromScreenYCor(mouseY))

    val precision = (-StrictMath.floor(StrictMath.log(firstCor - secondCor) / StrictMath.log(10))).toInt
    val baseLabel = JDouble.toString(Approximate.approximate(mouseCor, StrictMath.min(precision, 34)))

    val label =
      if (precision <= 0)
        baseLabel.substring(0, baseLabel.length - 2) // strip the trailing ".0"
      else
        baseLabel

    val labelWidth = getFontMetrics(getFont()).stringWidth(label)

    (label, labelWidth)

  }

}
