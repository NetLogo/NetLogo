// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

case class WorldDimensions(
    var minPxcor: Int, var maxPxcor: Int,
    var minPycor: Int, var maxPycor: Int) {
  def width = maxPxcor - minPxcor + 1
  def height = maxPycor - minPycor + 1
}
object WorldDimensions {
  def square(max: Int) =
    WorldDimensions(-max, max, -max, max)
}

