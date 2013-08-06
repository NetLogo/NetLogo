// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ LogoList, LogoListBuilder, Syntax }
import org.nlogo.nvm.{ Context, Reporter }
import collection.JavaConverters._

class _shapes extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.ListType)
  override def report(context: Context): LogoList = {
    val result = new LogoListBuilder
    for(shape <- world.turtleShapeList.getShapes.asScala)
      result.add(shape.getName)
    result.toLogoList
  }
}
