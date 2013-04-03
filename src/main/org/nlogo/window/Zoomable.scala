// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

trait Zoomable extends javax.swing.JComponent {
  val zoomer = new Zoomer(this)
  private var _zoomSteps = 0
  def zoomSteps = _zoomSteps
  def zoomMin: Int = -9
  def zoomFactor = 1.0 + 0.1 * zoomSteps
  def zoomSubcomponents = true
  def zoomTarget: java.awt.Component = this
}
