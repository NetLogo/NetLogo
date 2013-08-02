// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/** for wrapping up dimensions to resize the world using WorldResizer */

class WorldDimensions(var minPxcor: Int, var maxPxcor: Int,
                      var minPycor: Int, var maxPycor: Int) {
  def width = maxPxcor - minPxcor + 1
  def height = maxPycor - minPycor + 1
}
