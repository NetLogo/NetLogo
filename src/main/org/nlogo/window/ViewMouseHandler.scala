// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Point
import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.JComponent
import org.nlogo.api.{ AgentException, Approximate, ViewSettings, World }
import org.nlogo.awt.Mouse
import org.nlogo.util.Exceptions

class ViewMouseHandler(parent: JComponent, world: World, settings: ViewSettings) extends MouseAdapter {
  var mouseDown = false
  var mouseXCor = 0: Double
  var mouseYCor = 0: Double

  private var _mouseInside = false
  def mouseInside = _mouseInside

  def resetMouseCors() = {
    mouseXCor = 0
    mouseYCor = 0
  }

  private var pt: Point = null

  def updateMouseCors() = if(mouseInside && (pt != null)) translatePointToXCorYCor(pt)

  override def mousePressed(e: MouseEvent) =
    if(!e.isPopupTrigger && Mouse.hasButton1(e)) mouseDown = true

  override def mouseReleased(e: MouseEvent) =
    if(!e.isPopupTrigger && Mouse.hasButton1(e))
      mouseDown = false

  override def mouseExited(e: MouseEvent) = _mouseInside = parent.contains(e.getPoint)

  override def mouseDragged(e: MouseEvent) = {
    // technically this is redundant, we should already have gotten
    // a mousePressed, but just in case we didn't in some buggy VM,
    // we do this for good measure... - ST 10/5/04
    mouseDown = true
    // if we press the mouse button inside and then drag outside, we
    // still get mouseDragged events even though the mouse isn't inside
    // us anymore, so unlike in the mouseMoved case, we need this next check
    if(parent.contains(e.getPoint)) {
      updateMouseInside(e.getPoint)
      translatePointToXCorYCor(e.getPoint)
      pt = e.getPoint
    }
  }

  override def mouseMoved(e: MouseEvent) = {
    updateMouseInside(e.getPoint)
    translatePointToXCorYCor(e.getPoint)
    pt = e.getPoint
  }

  private def updateMouseInside(p: Point) = {
    val x1 = translatePointToUnboundedX(p.x)
    val y1 = translatePointToUnboundedY(p.y)
    try
      _mouseInside = (world.wrapX(x1) == x1 && world.wrapY(y1) == y1)
    catch {
      case e: AgentException => _mouseInside = false
    }
  }

  def translatePointToUnboundedX(x: Int) = {
    val rect = parent.getBounds()
    val xOff = settings.viewOffsetX
    val dx = x.toDouble / rect.width
    var xcor = (dx * settings.viewWidth) + (world.minPxcor - 0.5)
    xcor += xOff

    try
      xcor = world.wrapX(xcor)
    catch {
      case e: AgentException => Exceptions.ignore(e)
    }

    if (settings.patchSize <= 1.0)
      xcor = Approximate.approximate(xcor, 0)

    xcor
  }

  def translatePointToUnboundedY(y: Int) = {
    val rect = parent.getBounds()

    val yOff = settings.viewOffsetY
    val dy = y.toDouble / rect.height
    var ycor = world.maxPycor + 0.4999999 - (settings.viewHeight * dy)
    ycor += yOff

    try 
      ycor = world.wrapY(ycor)
    catch {
      case e: AgentException => Exceptions.ignore(e)
    }

    if (settings.patchSize <= 1.0)
      ycor = Approximate.approximate(ycor, 0)

    ycor
  }

  def translatePointToXCorYCor(p: Point): Unit = {
    val rect = parent.getBounds()

    val minx = world.minPxcor
    val maxx = world.maxPxcor
    val miny = world.minPycor
    val maxy = world.maxPycor

    val xOff = settings.viewOffsetX
    val yOff = settings.viewOffsetY

    val dx = p.x.toDouble / rect.width
    var newMouseX = (dx * settings.viewWidth) + (minx - 0.5)
    newMouseX += xOff
    try
      newMouseX = world.wrapX(newMouseX)
    catch {
      case e: AgentException => return
    }

    if (newMouseX < minx - 0.5)
      newMouseX = minx - 0.5
    else if (newMouseX >= maxx + 0.5)
      newMouseX = maxx + 0.4999999

    if(settings.patchSize <= 1.0)
      newMouseX = Approximate.approximate(newMouseX, 0)

    val dy = p.y.toDouble / rect.height
    var newMouseY = maxy + 0.4999999 - (settings.viewHeight * dy)
    newMouseY += yOff
    try
      newMouseY = world.wrapY(newMouseY)
    catch {
      case e: AgentException => return
    }
    if (newMouseY < miny - 0.5)
      newMouseY = miny - 0.5
    else if (newMouseY >= maxy + 0.5)
      newMouseY = maxy + 0.4999999

    if (settings.patchSize <= 1.0)
      newMouseY = Approximate.approximate(newMouseY, 0)

    // update mouse coordinates
    mouseXCor = newMouseX
    mouseYCor = newMouseY
  }
}
