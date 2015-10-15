// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.LogoListBuilder
import org.nlogo.core.LogoList
import org.nlogo.nvm.{ Context, Reporter }

class _shapes extends Reporter {
  override def report(context: Context): LogoList = {
    val result = new LogoListBuilder
    for(shape <- world.turtleShapeList.shapes)
      result.add(shape.name)
    result.toLogoList
  }
}
