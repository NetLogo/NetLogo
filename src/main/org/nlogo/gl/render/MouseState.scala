// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import java.awt.Point

/** current mouse coordinates/status */
class MouseState {
  var on = false
  var inside = false
  var down = false
  var xcor = 0d
  var ycor = 0d
  var point: Point = null
  var pickRequest = false
}
