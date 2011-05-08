package org.nlogo.prim

import org.nlogo.agent.AgentSet
import org.nlogo.nvm.{Context, Reporter, Syntax}

class _patches extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.TYPE_PATCHSET)
  override def report(context: Context) =
    report_1(context)
  def report_1(context: Context): AgentSet =
    world.patches()
}
