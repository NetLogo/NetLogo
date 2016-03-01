// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape

import org.scalatest.Assertions
import java.awt.Color

object TestHelpers {

  import Assertions._

  def makeSquarePolygon(recolorable: Boolean, name: String = "test", rotatable: Boolean = true) = {
    val n = name
    val r = rotatable
    val shape = new org.nlogo.shape.VectorShape() {
      setEditableColorIndex(if (recolorable) 15 else 0) // crazy? maybe. 15 is white. 0 is gray.
      name = n
      setRotatable(r)
      // a non recolorable white 'polygon' that is really just a square.
      // Squares alone (not part of a nested shape) arent cachable. Polygons are.
      // (-1 for white. for red use -2674135)
      val xs = List(45, 45, 255, 255, 45)
      val ys = List(45, 255, 255, 45, 45)
      val whitePolygon = new Polygon(xs, ys, Color.decode("-1"))
      whitePolygon.filled = true
      whitePolygon.marked = recolorable
      addElement(whitePolygon)
    }
    assert(shape.fgRecolorable === recolorable)
    shape
  }

}
