// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

case class WorldDimensions(
    minPxcor: Int, maxPxcor: Int,
    minPycor: Int, maxPycor: Int,
    patchSize: Double = 12.0,
    wrappingAllowedInX: Boolean = true,
    wrappingAllowedInY: Boolean = true) {
  def this(minPxcor: Int, maxPxcor: Int, minPycor: Int, maxPycor: Int) = {
    this(minPxcor, maxPxcor, minPycor, maxPycor, 12.0, true, true)
  }
  def width = maxPxcor - minPxcor + 1
  def height = maxPycor - minPycor + 1

}

/** for wrapping up dimensions to resize the world using WorldResizer
 *  This class is shared between NetLogo and NetLogo-Headless.
 *  That *does not* mean that it is available for use in Headless.
 *  Headless *does not* support 3D at this time (and may never support 3D).
 */

object WorldDimensions3D {
  def box(n: Int) = new WorldDimensions3D(-n, n, -n, n, -n, n)
}

class WorldDimensions3D(minPxcor: Int, maxPxcor: Int,
                        minPycor: Int, maxPycor: Int,
                        var minPzcor: Int, var maxPzcor: Int,
                        patchSize: Double = 12.0,
                        wrappingAllowedInX: Boolean = true,
                        wrappingAllowedInY: Boolean = true,
                        val wrappingAllowedInZ: Boolean = true)
extends WorldDimensions(minPxcor, maxPxcor, minPycor, maxPycor, patchSize, wrappingAllowedInX, wrappingAllowedInY) {
  def copy(minPxcor: Int, maxPxcor: Int, minPycor: Int, maxPycor: Int, minPzcor: Int, maxPzcor: Int): WorldDimensions3D = {
    new WorldDimensions3D(minPxcor, maxPxcor, minPycor, maxPycor, minPzcor, maxPzcor)
  }

  // can't define default arguments in more than one method of the same name
  // and that's done in core.WorldDimensions
  def copyThreeD(
    minPxcor: Int = minPxcor,
    maxPxcor: Int = maxPxcor,
    minPycor: Int = minPycor,
    maxPycor: Int = maxPycor,
    minPzcor: Int = minPzcor,
    maxPzcor: Int = maxPzcor,
    patchSize: Double = patchSize,
    wrappingAllowedInX: Boolean = wrappingAllowedInX,
    wrappingAllowedInY: Boolean = wrappingAllowedInY,
    wrappingAllowedInZ: Boolean = wrappingAllowedInZ): WorldDimensions3D = {
    new WorldDimensions3D(minPxcor, maxPxcor, minPycor, maxPycor, minPzcor, maxPzcor, patchSize, wrappingAllowedInX, wrappingAllowedInY, wrappingAllowedInZ)
  }

  override def toString: String = {
    s"WorldDimensions3D($minPxcor, $maxPxcor, $minPycor, $maxPycor, $minPzcor, $maxPzcor, $patchSize, $wrappingAllowedInX, $wrappingAllowedInY, $wrappingAllowedInZ)"
  }

}
