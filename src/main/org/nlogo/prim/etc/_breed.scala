// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.AgentSet
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Reporter, Context }

class _breed(breedName: String) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.TurtlesetType)
  override def toString =
    super.toString + ":" + breedName
  override def report(context: Context): AgentSet =
    report_1(context)
  def report_1(context: Context): AgentSet =
    world.getBreed(breedName)
}
