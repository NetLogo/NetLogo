// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _worldwidth extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.NumberType)
  override def report(context: Context) =
    report_1(context)
  def report_1(context: Context): java.lang.Double =
    world.worldWidthBoxed
  def report_2(context: Context): Double =
    world.worldWidth
}
