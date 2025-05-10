// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.LogoList
import org.nlogo.nvm.{ Context, Reporter }

class _shapes extends Reporter {
  def report(context: Context): AnyRef =
    LogoList(world.turtleShapeList.shapes.map(_.name)*)
}
