// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.WorldDimensions

/** for wrapping up dimensions to resize the world using WorldResizer */

class WorldDimensions3D(minPxcor: Int, maxPxcor: Int,
                        minPycor: Int, maxPycor: Int,
                        var minPzcor: Int, var maxPzcor: Int)
extends WorldDimensions(minPxcor, maxPxcor, minPycor, maxPycor) {
  def copy(minPxcor: Int, maxPxcor: Int, minPycor: Int, maxPycor: Int, minPzcor: Int, maxPzcor: Int): WorldDimensions3D = {
    new WorldDimensions3D(minPxcor, maxPxcor, minPycor, maxPycor, minPzcor, maxPzcor)
  }
}
