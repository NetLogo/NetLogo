// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.api.{ LogoList, LogoListBuilder }
import org.nlogo.nvm.{ Context, Reporter }

class _linkshapes extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.ListType)
  override def report(context: Context): LogoList = {
    val result = new LogoListBuilder
    import collection.JavaConverters._
    for(shape <- world.linkShapeList.getShapes.asScala)
      result.add(shape.getName)
    result.toLogoList
  }
}
