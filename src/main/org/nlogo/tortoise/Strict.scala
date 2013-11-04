// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

// ADDING SOMETHING TO THIS OBJECT?
// WELL, ARE YA, PUNK?
// If you are, open your browser's console and type in "Math.<the name of that thing>".
// If it returns `undefined`, you need to add it to 'compat.coffee', too!
// Or, maybe just consult this page to figure out if it's a part of the spec or not:
// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Math
// --JAB (11/4/13)
object Strict {
  val PI = StrictMath.PI
  def abs(x: Double) = StrictMath.abs(x)
  def sin(x: Double) = StrictMath.sin(x)
  def cos(x: Double) = StrictMath.cos(x)
  def atan2(x: Double, y: Double) = StrictMath.atan2(x, y)
  def toRadians(x: Double) = StrictMath.toRadians(x)
  def round(x: Double) = StrictMath.round(x)
  def floor(x: Double) = StrictMath.floor(x)
  def sqrt(x: Double) = StrictMath.sqrt(x)
  def pow(x: Double, y: Double) = StrictMath.pow(x, y)
  def toDegrees(x: Double) = StrictMath.toDegrees(x)
}
