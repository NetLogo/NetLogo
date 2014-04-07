// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

case class WorldDimensions(
    minPxcor: Int, maxPxcor: Int,
    minPycor: Int, maxPycor: Int,
    patchSize: Double = 12.0,
    wrappingAllowedInY: Boolean = true,
    wrappingAllowedInX: Boolean = true) {
  def this(minPxcor: Int, maxPxcor: Int, minPycor: Int, maxPycor: Int) {
    this(minPxcor, maxPxcor, minPycor, maxPycor, 12.0, true, true)
  }
  def width = maxPxcor - minPxcor + 1
  def height = maxPycor - minPycor + 1
}
object WorldDimensions {
  def square(max: Int) =
    WorldDimensions(-max, max, -max, max)
}

