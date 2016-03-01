// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ AgentSet, Turtle }
import org.nlogo.api.{ LogoException, Syntax }
import org.nlogo.nvm.{ Context, Reporter }

class _linkneighbors(breedName: String) extends Reporter {
  def this() = this(null)

  override def syntax =
    Syntax.reporterSyntax(Syntax.AgentsetType, "-T--")

  override def toString = s"${super.toString}:$breedName"

  override def report(context: Context) = report_1(context)

  def report_1(context: Context): AgentSet = {
    val breed = if (breedName == null) world.links else world.getLinkBreed(breedName)
    mustNotBeDirected(breed, context)
    world.linkManager.findLinkedWith(context.agent.asInstanceOf[Turtle], breed)
  }
}
