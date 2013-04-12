// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Reporter, Context, EngineException }
import org.nlogo.agent.{ Turtle, LinkManager }

class _outlinkneighbor(breedName: String) extends Reporter {

  def this() = this(null)

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.AgentType),
      Syntax.BooleanType, "-T--")

  override def toString =
    super.toString + ":" + breedName

  override def report(context: Context) = {
    val target = argEvalTurtle(context, 0)
    val breed =
      if (breedName == null)
        world.links
      else
        world.getLinkBreed(breedName)
    for(err <- LinkManager.mustNotBeUndirected(breed))
      throw new EngineException(context, this, err)
    Boolean.box(null !=
      world.linkManager.findLinkFrom(
        context.agent.asInstanceOf[Turtle], target, breed, true))
  }

}
