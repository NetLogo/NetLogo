// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ WorldDimensions3D => CoreWorldDimensions3D }

/** This class is deprecated. Use org.nlogo.core.WorldDimensions instead.
 *  Headless *does not* support 3D at this time (and may never support 3D).
 */

object WorldDimensions3D {
  @deprecated("Use org.nlogo.core.WorldDimensions3D.box instead", "6.1.0")
  def box(n: Int) = new WorldDimensions3D(-n, n, -n, n, -n, n)
}

@deprecated("Use org.nlogo.core.WorldDimensions3D instead", "6.1.0")
class WorldDimensions3D(_minPxcor: Int, _maxPxcor: Int,
                        _minPycor: Int, _maxPycor: Int,
                        _minPzcor: Int, _maxPzcor: Int,
                        patchSize: Double = 12.0,
                        wrappingAllowedInX: Boolean = true,
                        wrappingAllowedInY: Boolean = true,
                        wrappingAllowedInZ: Boolean = true)
extends CoreWorldDimensions3D(
  _minPxcor, _maxPxcor,
  _minPycor, _maxPycor,
  _minPzcor, _maxPzcor,
  patchSize,
  wrappingAllowedInX, wrappingAllowedInY, wrappingAllowedInZ) {

  override def copy(minPxcor: Int, maxPxcor: Int, minPycor: Int, maxPycor: Int, minPzcor: Int, maxPzcor: Int): WorldDimensions3D = {
    new WorldDimensions3D(minPxcor, maxPxcor, minPycor, maxPycor, minPzcor, maxPzcor)
  }

  // can't define default arguments in more than one method of the same name
  // and that's done in core.WorldDimensions
  override def copyThreeD(
    _minPxcor: Int = minPxcor,
    _maxPxcor: Int = maxPxcor,
    _minPycor: Int = minPycor,
    _maxPycor: Int = maxPycor,
    _minPzcor: Int = minPzcor,
    _maxPzcor: Int = maxPzcor,
    patchSize: Double = patchSize,
    wrappingAllowedInX: Boolean = wrappingAllowedInX,
    wrappingAllowedInY: Boolean = wrappingAllowedInY,
    wrappingAllowedInZ: Boolean = wrappingAllowedInZ): WorldDimensions3D = {
    new WorldDimensions3D(_minPxcor, _maxPxcor, _minPycor, _maxPycor, _minPzcor, _maxPzcor, patchSize, wrappingAllowedInX, wrappingAllowedInY, wrappingAllowedInZ)
  }
}
