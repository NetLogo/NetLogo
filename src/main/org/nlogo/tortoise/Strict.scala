// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

object Strict {
  def abs(x: Double) = StrictMath.abs(x)
  def sin(x: Double) = StrictMath.sin(x)
  def cos(x: Double) = StrictMath.cos(x)
  def toRadians(x: Double) = StrictMath.toRadians(x)
  def round(x: Double) = StrictMath.round(x)
  def sqrt(x: Double) = StrictMath.sqrt(x)
  def pow(x: Double, y: Double) = StrictMath.pow(x, y)
}
