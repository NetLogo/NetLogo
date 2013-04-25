// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.nlogo.api.PlotPenInterface

class PlotPainter(plot: Plot) {

  private var gOff: java.awt.Graphics = null
  private var offScreenImage: java.awt.image.BufferedImage = null

  private var height = 0
  private var width = 0

  def setupOffscreenImage(width: Int, height: Int) {
    if(offScreenImage == null || this.width != width || this.height != height) {
      this.width = width
      this.height = height
      if(offScreenImage != null) {
        offScreenImage.flush()
        offScreenImage = null
      }
      offScreenImage = new java.awt.image.BufferedImage(
        width, height,
        // hopefully this pixel format is efficient on all platforms - ST 6/4/05
        java.awt.image.BufferedImage.TYPE_INT_ARGB)
      if(gOff != null)
        gOff.dispose()
      gOff = offScreenImage.getGraphics()
      gOff.setColor(java.awt.Color.WHITE)
      gOff.fillRect(0, 0, width, height)
      refresh()
    }
  }

  def drawImage(g: java.awt.Graphics) { g.drawImage(offScreenImage, 0, 0, null) }

  def refresh() {
    gOff.setColor(java.awt.Color.WHITE)
    gOff.fillRect(0, 0, offScreenImage.getWidth, offScreenImage.getHeight)
    for(pen <- plot.pens; if !pen.state.hidden)
      refreshPen(pen, collectPointsForPainting(pen))
  }

  /// at painting time, we need to convert each bar to four points
  private def collectPointsForPainting(pen: PlotPen): Seq[PlotPoint] = {
    pen.state.mode match {
      case PlotPenInterface.PointMode | PlotPenInterface.LineMode =>
        pen.points
      case PlotPenInterface.BarMode =>
        pen.points.flatMap(old =>
          Seq(old.copy(y = 0, isDown = true),
              old.copy(isDown = true),
              old.copy(x = old.x + pen.state.interval, isDown = true),
              old.copy(x = old.x + pen.state.interval, y = 0, isDown = true)))
    }
  }

  // In a model that plots a very large number of points, this
  // method can account for a substantial amount of the runtime,
  // so it's worth considering even small efficiency issues here. - ST 8/16/07
  private def refreshPen(pen: PlotPen, pointsToPlot: Seq[PlotPoint]) {
    var last: PlotPoint = null
    // used to cut down on unnecessary setColor() calls - ST 9/17/03
    var color = 0
    // these three variables are used to cut down on unnecessary drawLine calls
    // when in line mode by "coalescing" multiple points that all have the
    // same screen xcor - ST 9/17/03
    var (minY, maxY) = (0, 0)
    var coalescing = false
    gOff.asInstanceOf[java.awt.Graphics2D].setRenderingHint(
      java.awt.RenderingHints.KEY_ANTIALIASING,
      if(pen.state.mode == PlotPenInterface.PointMode) java.awt.RenderingHints.VALUE_ANTIALIAS_OFF
      else java.awt.RenderingHints.VALUE_ANTIALIAS_ON)

    val size = pointsToPlot.size
    for(i <- 0 until size) {
      val next = pointsToPlot(i)
      if(next.color != color) {
        color = next.color
        gOff.setColor(new java.awt.Color(color))
      }
      if(pen.state.mode == PlotPenInterface.PointMode) { drawPoint(gOff, next) }
      else{ // line mode or bar mode
        if(last == null) {
          // it would seem to make more sense to call drawPoint here,
          // but we don't want a 2x2 pixel point, otherwise we get
          // little hangnails at the beginning of a series of
          // connected segments - ST 9/18/03
          drawEdge(gOff, next, next)
        }
        else{
          // check: can we coalesce?
          if(i != size - 1 &&
            screenX(last.x) == screenX(next.x) &&
            last.color == next.color &&
            last.isDown &&
            next.isDown) {
            val y = screenY(next.y)
            if(coalescing) {
              // continue coalescing
              if(y < minY) { minY = y }
              if(y > maxY) { maxY = y }
            }
            else{
              gOff.asInstanceOf[java.awt.Graphics2D].setRenderingHint(
                java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_OFF)
              // begin coalescing
              drawEdge(gOff, last, next)
              coalescing = true
              minY = y
              maxY = y
            }
          }
          else{
            if(coalescing) {
              // done coalescing, draw a single line representing
              // all of the coalesced points
              gOff.setColor(new java.awt.Color(last.color))
              gOff.fillRect(screenX(last.x), minY, 1, maxY - minY + 1)
              coalescing = false
              gOff.asInstanceOf[java.awt.Graphics2D].setRenderingHint(
                java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
              gOff.setColor(new java.awt.Color(color))
            }
            // draw the new point
            if(next.isDown) { drawEdge(gOff, last, next) }
          }
        }
      }
      last = next
    }
  }

  /// private helpers

  private def drawPoint(g: java.awt.Graphics, p: PlotPoint) {
    g.drawRect(screenX(p.x), screenY(p.y), 1, 1)
  }

  private def drawEdge(g: java.awt.Graphics, p1: PlotPoint, p2: PlotPoint) {
    val x1 = screenX(p1.x)
    val y1 = screenY(p1.y)
    val x2 = screenX(p2.x)
    val y2 = screenY(p2.y)
    // trying to draw a bunch of out of range lines is for some reason very slow on some Windows
    // machines so pre-check the bounds ev 8/28/07
    if(! ((x1 < 0 && x2 < 0) ||
          (x1 > width && x2 > width) ||
          (y1 < 0 && y2 < 0) ||
          (y1 > height && y2 > height)))
      g.drawLine(x1, y1, x2, y2)
  }

  // in screenX and screenY, we need to watch out for
  // bugs.sun.com/bugdatabase/view_bug.do?bug_id=4249708 if x and y are way out of range --
  // e.g. if someone wants to draw the x axis and so does something like "plotxy 0 0 plotxy 10000000
  // 0".  java.awt.Point is documented as using 32 bit ints, but in practice, not all of that range
  // is usable on every VM and OS.  I've seen this problem on Java 1.1 VM's; I don't think I've seen
  // it on newer VM's, but the bug parade doesn't say it's fixed, so let's be safe.  Not sure
  // exactly what the usable range is, but +/- 16383 seems like a good guess. - ST 3/12/03, 8/1/03
  private def screenX(x: Double): Int = {
    val range = plot.state.xMax - plot.state.xMin
    val scale = range / (width - 1)
    screen(StrictMath.rint((x - plot.state.xMin) / scale))
  }

  private def screenY(y: Double): Int = {
    val range = plot.state.yMax - plot.state.yMin
    val scale = range / (height - 1)
    screen(StrictMath.rint(height - 1 - ((y - plot.state.yMin) / scale)))
  }

  private def screen(p: Double) = if(p > 16383) 16383 else if(p < -16383) -16383 else p.toInt
}
