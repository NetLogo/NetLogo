// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.AgentSet
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _breed(breedName: String) extends Reporter {
  override def syntax: Syntax = Syntax.reporterSyntax(Syntax.TurtlesetType)

  override def toString: String = s"${super.toString}:$breedName"

  override def report(context: Context) = report_1(context)

  def report_1(context: Context): AgentSet = world.getBreed(breedName)
}
