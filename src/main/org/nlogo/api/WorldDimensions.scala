// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

case class WorldDimensions(
    minPxcor: Int, maxPxcor: Int,
    minPycor: Int, maxPycor: Int) {
  def width = maxPxcor - minPxcor + 1
  def height = maxPycor - minPycor + 1
}
object WorldDimensions {
  def square(max: Int) =
    WorldDimensions(-max, max, -max, max)
}

