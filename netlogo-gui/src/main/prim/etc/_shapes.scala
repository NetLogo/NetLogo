// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ LogoList, Shape }
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

import java.util.List;

class _shapes extends Reporter {
  def syntax = Syntax.reporterSyntax(ret = Syntax.ListType)

  def report(context: Context): AnyRef =
    LogoList(world.turtleShapeList.shapes.map(_.name): _*)
}
