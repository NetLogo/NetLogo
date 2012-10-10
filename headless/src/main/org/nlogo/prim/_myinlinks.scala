// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ AgentSet, Turtle }
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Reporter, Context }

class _myinlinks(breedName: String) extends Reporter {

  def this() = this(null)

  override def syntax =
    Syntax.reporterSyntax(Syntax.LinksetType, "-T--")

  override def toString =
    super.toString + ":" + breedName

  override def report(context: Context) = {
    val breed =
      if (breedName == null) world.links
      else world.getLinkBreed(breedName)
    mustNotBeUndirected(breed, context)
    world.linkManager.findLinksTo(
      context.agent.asInstanceOf[Turtle], breed)
  }

}
