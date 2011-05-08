package org.nlogo.prim

import org.nlogo.agent.AgentSet
import org.nlogo.nvm.{Context, Reporter, Syntax}

class _links extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.TYPE_LINKSET)
  override def report(context: Context) =
    report_1(context)
  def report_1(context: Context): AgentSet =
    world.links()
}
