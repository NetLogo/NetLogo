// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.internalapi

// would like to make this able to handle multiple mouse events
// eventually
trait GUIWorkspace {
  def mouseXCor:   Double
  def mouseYCor:   Double
  def mouseDown:   Boolean
  def mouseInside: Boolean
}

trait WritableGUIWorkspace extends GUIWorkspace {
  def setMouseDown(isDown: Boolean): Unit
  def setMouseInside(isInside: Boolean): Unit
  def setMouseCors(x: Double, y: Double): Unit
}
