// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, AgentKind }
import org.nlogo.agent.{ Turtle, AgentSet, LinkManager }
import org.nlogo.nvm.{ Reporter, Context, EngineException }

class _outlinkneighbors(breedName: String) extends Reporter {

  def this() = this(null)

  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.AgentsetType,
      agentClassString = "-T--")

  override def report(context: Context): AgentSet = {
    val breed =
      if (breedName == null)
        world.links
      else
        world.getLinkBreed(breedName)
    for(err <- LinkManager.mustNotBeUndirected(breed))
      throw new EngineException(context, this, err)
    AgentSet.fromIterator(AgentKind.Turtle,
      world.linkManager.findLinkedFrom(
        context.agent.asInstanceOf[Turtle], breed))
  }

}
