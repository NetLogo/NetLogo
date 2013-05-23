// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Reporter, Context, EngineException }
import org.nlogo.agent.{ Turtle, LinkManager }

class _linkneighbor(breedName: String) extends Reporter {

  def this() = this(null)

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.AgentType),
      Syntax.BooleanType, "-T--")

  override def toString =
    super.toString + ":" + breedName

  override def report(context: Context) =
    Boolean.box(report_1(context, argEvalTurtle(context, 0)))

  def report_1(context: Context, target: Turtle): Boolean = {
    val parent = context.agent.asInstanceOf[Turtle]
    val breed =
      if (breedName == null)
        world.links
      else
        world.getLinkBreed(breedName)
    for(err <- LinkManager.mustNotBeDirected(breed))
      throw new EngineException(context, this, err)
    world.linkManager.findLinkFrom(parent, target, breed, true) != null ||
      world.linkManager.findLinkFrom(target, parent, breed, true) != null
  }

}
