// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Turtle, AgentSet, LinkManager }
import org.nlogo.api, api.Syntax
import org.nlogo.nvm.{ Reporter, Context, EngineException }

class _linkneighbors(breedName: String) extends Reporter {

  def this() = this(null)

  override def syntax =
    Syntax.reporterSyntax(
      Syntax.AgentsetType, "-T--")

  override def toString =
    super.toString + ":" + breedName

  override def report(context: Context) =
    report_1(context)

  def report_1(context: Context): AgentSet = {
    val breed =
      if (breedName == null)
        world.links
      else
        world.getLinkBreed(breedName)
    for(err <- LinkManager.mustNotBeDirected(breed))
      throw new EngineException(context, this, err)
    AgentSet.fromIterator(api.AgentKind.Turtle,
      world.linkManager.findLinkedWith(
        context.agent.asInstanceOf[Turtle], breed))
  }

}
