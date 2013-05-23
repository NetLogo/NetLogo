// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ AgentSet, Turtle, LinkManager }
import org.nlogo.api, api.Syntax
import org.nlogo.nvm.{ Reporter, Context, EngineException }

class _myoutlinks(breedName: String) extends Reporter {

  def this() = this(null)

  override def syntax =
    Syntax.reporterSyntax(Syntax.LinksetType, "-T--")

  override def toString =
    super.toString + ":" + breedName

  override def report(context: Context): AgentSet = {
    val breed =
      if (breedName == null) world.links
      else world.getLinkBreed(breedName)
    for(err <- LinkManager.mustNotBeUndirected(breed))
      throw new EngineException(context, this, err)
    AgentSet.fromIterator(api.AgentKind.Link,
      world.linkManager.findLinksFrom(
        context.agent.asInstanceOf[Turtle], breed))
  }

}
