// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import org.nlogo.api.{ AgentFollowingPerspective, Perspective }
import java.awt.event.{ MouseEvent, MouseWheelEvent }
import org.nlogo.awt.Mouse.hasButton1

object MouseMotionHandler {
  sealed trait Mode
  case object OrbitMode extends Mode
  case object ZoomMode extends Mode
  case object TranslateMode extends Mode
  case object InterfaceMode extends Mode
}

class MouseMotionHandler(view: View)
extends java.awt.event.MouseListener
with java.awt.event.MouseMotionListener
with java.awt.event.MouseWheelListener {

  import MouseMotionHandler._

  private var movementMode: Mode = OrbitMode
  private var prevMouseX, prevMouseY = 0
  val world = view.viewManager.world

  def setMovementMode(mode: Mode): Unit = {
    if (mode == InterfaceMode) {
      view.renderer.setMouseMode(true)
      view.display()
    }
    else if (movementMode == InterfaceMode)
      view.renderer.setMouseMode(false)
    movementMode = mode
  }

  // MouseListener

  def mouseEntered(evt: MouseEvent): Unit = {
    if (movementMode == InterfaceMode)
      view.renderer.mouseInside(evt.getX, evt.getY)
  }

  def mouseExited(evt: MouseEvent): Unit = {
    if (movementMode == InterfaceMode)
      view.renderer.mouseInside(evt.getX, evt.getY)
  }

  def mousePressed(evt: MouseEvent): Unit = {
    prevMouseX = evt.getX
    prevMouseY = evt.getY
    if (!evt.isPopupTrigger && movementMode == InterfaceMode && hasButton1(evt))
      view.renderer.mouseDown(true)
    else {
      if (evt.isPopupTrigger)
        view.doPopup(evt)
      view.renderer.showCrossHairs(true)
    }
  }

  def mouseReleased(evt: MouseEvent): Unit = {
    view.renderer.showCrossHairs(false)
    if (!evt.isPopupTrigger && (movementMode == InterfaceMode) && hasButton1(evt))
      view.renderer.mouseDown(false)
    else if (evt.isPopupTrigger)
      view.doPopup(evt)
    view.display()
  }

  def mouseWheelMoved(e: MouseWheelEvent): Unit = {
    val observer = world.observer
    var zoomDist = -e.getUnitsToScroll.toDouble
    observer.perspective match {
      case afp: AgentFollowingPerspective =>
        zoomDist = zoomDist min afp.followDistance
        val newDist = (afp.followDistance - zoomDist).toInt
        // slider values from ViewControlToolBar
        if (newDist <= 100)
          observer.setPerspective(Perspective.Follow(afp.targetAgent, newDist))
      case _ =>
        val orientation = observer.orientation.get
        val dist = orientation.dist
        zoomDist = zoomDist min dist
        observer.oxyandzcor(
          observer.oxcor + (zoomDist * orientation.dx),
          observer.oycor + (zoomDist * orientation.dy),
          observer.ozcor - (zoomDist * orientation.dz))
    }
    view.display()
  }

  /// Implementation of java.awt.event.MouseMotionListener

  def mouseDragged(evt: MouseEvent): Unit = {
    if (movementMode == InterfaceMode) {
      // we skip all the unnecessary computations below because it drastically slows down the
      // mouse updates jrn 5/20/05
      view.renderer.mouseDown(true)
      view.renderer.setMouseCors(evt.getPoint)
      view.renderer.mouseInside(evt.getX, evt.getY)
    }
    else handleDrag(evt)
  }

  private def handleDrag(evt: MouseEvent): Unit = {
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
    import observer.{ oxcor, oycor, ozcor }

    observer.perspective match {
      case afp: AgentFollowingPerspective =>
        val newDist = (afp.followDistance - thetaY).toInt
        // slider values from ViewControlToolBar
        if (newDist >= 0 && newDist <= 100)
          observer.setPerspective(Perspective.Follow(afp.targetAgent, newDist))
      case _ =>
        mode match {
          case OrbitMode =>
            observer.orbitRight(-thetaX)
            observer.orbitUp(-thetaY)
          case ZoomMode =>
            val orientation = observer.orientation.get
            if (thetaY < orientation.dist)
              observer.oxyandzcor(oxcor + (thetaY * orientation.dx),
                                  oycor + (thetaY * orientation.dy),
                                  ozcor - (thetaY * orientation.dz))
          case TranslateMode =>
            observer.translate(thetaX, thetaY)
          case InterfaceMode =>
            // do nothing
        }
    }
    view.display()
  }

  def mouseMoved(evt: MouseEvent): Unit = {
    if (movementMode == InterfaceMode) {
      view.renderer.setMouseCors(evt.getPoint)
      view.renderer.mouseInside(evt.getX, evt.getY)
    }
  }

  def mouseClicked(evt: MouseEvent): Unit = {}

}
