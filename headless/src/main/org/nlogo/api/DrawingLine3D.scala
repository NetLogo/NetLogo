// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait DrawingLine3D {
  def width: Double
  def x0: Double
  def y0: Double
  def z0: Double
  def x1: Double
  def y1: Double
  def z1: Double
  def color: AnyRef
  def length: Double
  def heading: Double
  def pitch: Double
}
