// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Turtle, AgentSet }
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Reporter, Context }

class _outlinkneighbors(breedName: String) extends Reporter {

  def this() = this(null)

  override def syntax =
    Syntax.reporterSyntax(
      Syntax.AgentsetType, "-T--")

  override def report(context: Context): AgentSet = {
    val breed =
      if (breedName == null)
        world.links
      else
        world.getLinkBreed(breedName)
    mustNotBeUndirected(breed, context)
    world.linkManager.findLinkedFrom(
      context.agent.asInstanceOf[Turtle], breed)
  }

}
