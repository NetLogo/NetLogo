// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.optimize

import org.nlogo.prim._
import org.nlogo.compile.api.{ Match, RewritingReporterMunger }

// _inradius(x: breed|turtles|patches, y) => _inradiusboundingbox(x, y)
object InRadiusBoundingBox extends RewritingReporterMunger {
  val clazz = classOf[_inradius]
  def munge(root: Match) {
    val arg0 = root.matchArg(0, classOf[_turtles], classOf[_patches], classOf[_breed])
    val arg1 = root.matchArg(1)
    root.strip()
    root.replace(classOf[_inradiusboundingbox])
    root.graftArg(arg0)
    root.graftArg(arg1)
  }
}
