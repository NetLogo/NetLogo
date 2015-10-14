// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.agent.{ Link, Turtle }
import org.nlogo.nvm.{ Context, Reporter }

class _end1 extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.TurtleType, "---L")
  override def report(context: Context) =
    report_1(context)
  def report_1(context: Context): Turtle =
    context.agent.asInstanceOf[Link].end1
}
