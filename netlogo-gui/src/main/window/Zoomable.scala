// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

trait Zoomable extends javax.swing.JComponent
with Events.ZoomedEvent.Handler {
  val zoomer = new Zoomer(this)
  private var _zoomSteps = 0
  def zoomSteps = _zoomSteps
  def zoomMin: Int = -9
  def zoomFactor = 1.0 + 0.1 * zoomSteps
  def zoomSubcomponents = true
  def zoomTarget: java.awt.Component = this
  def handle(e: org.nlogo.window.Events.ZoomedEvent) {
    if (isShowing) { // ignore unless we're the front tab
      val oldFactor = zoomFactor
      _zoomSteps = e.action match {
        case -1 =>
          zoomSteps - 1
        case  1 =>
          zoomSteps + 1
        case  0 =>
          0
      }
      _zoomSteps = _zoomSteps max zoomMin
      zoomer.scaleComponentFont(zoomTarget, zoomFactor, oldFactor, true)
      revalidate()
    }
  }
}
