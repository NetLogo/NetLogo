package org.nlogo.prim

import org.nlogo.agent.AgentSet
import org.nlogo.nvm.{Context, Reporter, Syntax}

class _turtles extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.TYPE_TURTLESET)
  override def report(context: Context): AnyRef =
    report_1(context)
  def report_1(context: Context): AgentSet =
    world.turtles()
}
