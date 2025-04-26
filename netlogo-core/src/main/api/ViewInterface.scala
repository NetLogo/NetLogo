// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.Shape

trait ViewInterface {
  def viewIsVisible: Boolean
  def framesSkipped(): Unit
  def isDead: Boolean
  def paintImmediately(force: Boolean): Unit
  def incrementalUpdateFromEventThread(): Unit
  def repaint(): Unit
  def mouseXCor: Double
  def mouseYCor: Double
  def mouseDown: Boolean
  def mouseInside: Boolean
  def resetMouseCors(): Unit
  def shapeChanged(shape: Shape): Unit
  def applyNewFontSize(fontSize: Int, zoom: Int): Unit
}
