// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/** for wrapping up dimensions to resize the world using WorldResizer */

class WorldDimensions3D(minPxcor: Int, maxPxcor: Int,
                        minPycor: Int, maxPycor: Int,
                        var minPzcor: Int, var maxPzcor: Int)
extends WorldDimensions(minPxcor, maxPxcor, minPycor, maxPycor)
