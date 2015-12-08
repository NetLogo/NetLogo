// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.AgentSet
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _linkbreed(breedName: String) extends Reporter {
  override def toString: String = s"${super.toString}:$breedName"

  override def syntax: Syntax = Syntax.reporterSyntax(Syntax.LinksetType)

  override def report(context: Context): AnyRef = world.getLinkBreed(breedName)

  def report_1(context: Context): AgentSet = world.getLinkBreed(breedName)
}
