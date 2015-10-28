// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape

import org.scalatest.Assertions

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
      val p = new Polygon(List(45, 45, 255, 255, 45), List(45, 255, 255, 45, 45), new java.awt.Color(255, 255, 255))
      p.filled = true
      p.marked = recolorable
      addElement(p)
    }
    assert(shape.fgRecolorable === recolorable)
    shape
  }

}
