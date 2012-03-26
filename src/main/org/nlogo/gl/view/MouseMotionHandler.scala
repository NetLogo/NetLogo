// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import org.nlogo.api.Perspective
import java.awt.event.{ MouseEvent, MouseWheelEvent }
import org.nlogo.awt.Mouse.hasButton1

object MouseMotionHandler {
  sealed trait Mode
  case object OrbitMode extends Mode
  case object ZoomMode extends Mode
  case object TranslateMode extends Mode
  case object InteractMode extends Mode
}

class MouseMotionHandler(view: View)
extends java.awt.event.MouseListener
with java.awt.event.MouseMotionListener
with java.awt.event.MouseWheelListener {

  import MouseMotionHandler._

  private var movementMode: Mode = OrbitMode
  private var prevMouseX, prevMouseY = 0
  val world = view.viewManager.world

  def setMovementMode(mode: Mode) {
    if (mode == InteractMode) {
      view.renderer.setMouseMode(true)
      view.signalViewUpdate()
    }
    else if (movementMode == InteractMode)
      view.renderer.setMouseMode(false)
    movementMode = mode
  }

  // MouseListener

  def mouseEntered(evt: MouseEvent) {
    if (movementMode == InteractMode)
      view.renderer.mouseInside(evt.getX, evt.getY)
  }

  def mouseExited(evt: MouseEvent) {
    if (movementMode == InteractMode)
      view.renderer.mouseInside(evt.getX, evt.getY)
  }

  def mousePressed(evt: MouseEvent) {
    prevMouseX = evt.getX
    prevMouseY = evt.getY
    if (!evt.isPopupTrigger && movementMode == InteractMode && hasButton1(evt))
      view.renderer.mouseDown(true)
    else {
      if (evt.isPopupTrigger)
        view.doPopup(evt)
      view.renderer.showCrossHairs(true)
    }
  }

  def mouseReleased(evt: MouseEvent) {
    view.renderer.showCrossHairs(false)
    if (!evt.isPopupTrigger && (movementMode == InteractMode) && hasButton1(evt))
      view.renderer.mouseDown(false)
    else if (evt.isPopupTrigger)
      view.doPopup(evt)
    view.signalViewUpdate()
  }

  def mouseWheelMoved(e: MouseWheelEvent) {
    val observer = world.observer
    var zoomDist = -e.getUnitsToScroll.toDouble
    if (observer.perspective == Perspective.Follow || observer.perspective == Perspective.Ride) {
      zoomDist = zoomDist min observer.followDistance
      val newDist = (observer.followDistance - zoomDist).toInt
      // slider values from ViewControlToolBar
      if (newDist <= 100)
        observer.followDistance(newDist)
    }
    else {
      val dist = observer.dist
      zoomDist = zoomDist min dist
      observer.oxyandzcor(
        observer.oxcor + (zoomDist * observer.dx),
        observer.oycor + (zoomDist * observer.dy),
        observer.ozcor - (zoomDist * observer.dz))
    }
    view.signalViewUpdate()
  }

  /// Implementation of java.awt.event.MouseMotionListener

  def mouseDragged(evt: MouseEvent) {
    if (movementMode == InteractMode) {
      // we skip all the unnecessary computations below because it drastically slows down the
      // mouse updates jrn 5/20/05
      view.renderer.mouseDown(true)
      view.renderer.setMouseCors(evt.getPoint)
      view.renderer.mouseInside(evt.getX, evt.getY)
    }
    else handleDrag(evt)
  }

  private def handleDrag(evt: MouseEvent) {
    val mode =
      if (evt.isAltDown) TranslateMode
      else if (evt.isShiftDown) ZoomMode
      else movementMode
    val x = evt.getX
    val y = evt.getY

    val thetaX = (x - prevMouseX) / 2.0f
    val thetaY = (prevMouseY - y) / 2.0f

    prevMouseX = x
    prevMouseY = y

    val observer = world.observer
    import observer.{ oxcor, oycor, ozcor, dist }

    if (world.observer.perspective == Perspective.Follow || world.observer.perspective == Perspective.Ride) {
      val newDist = (observer.followDistance - thetaY).toInt
      // slider values from ViewControlToolBar
      if (newDist >= 0 && newDist <= 100)
        observer.followDistance(newDist)
      view.signalViewUpdate()
    }
    else {
      mode match {
        case OrbitMode =>
          observer.orbitRight(-thetaX)
          observer.orbitUp(-thetaY)
        case ZoomMode =>
          if (thetaY < dist)
            observer.oxyandzcor(oxcor + (thetaY * observer.dx),
                                oycor + (thetaY * observer.dy),
                                ozcor - (thetaY * observer.dz))
        case TranslateMode =>
          observer.translate(thetaX, thetaY)
        case InteractMode =>
          // do nothing
      }
      view.signalViewUpdate()
    }
  }

  def mouseMoved(evt: MouseEvent) {
    if (movementMode == InteractMode) {
      view.renderer.setMouseCors(evt.getPoint)
      view.renderer.mouseInside(evt.getX, evt.getY)
    }
  }

  def mouseClicked(evt: MouseEvent) {}

}
