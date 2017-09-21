// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.Shape

trait ViewInterface {
  def viewIsVisible: Boolean
  def framesSkipped(): Unit
  def isDead: Boolean
  def paintImmediately(force: Boolean)
  def incrementalUpdateFromEventThread()
  def repaint()
  def mouseXCor: Double
  def mouseYCor: Double
  def mouseDown: Boolean
  def mouseInside: Boolean
  def resetMouseCors()
  def shapeChanged(shape: Shape)
  def applyNewFontSize(fontSize: Int, zoom: Int)
  def freeze(): Unit
  def thaw(): Unit
}
