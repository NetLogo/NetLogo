// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ AgentSet, LinkManager, Turtle }
import org.nlogo.api, api.Syntax
import org.nlogo.nvm.{ Reporter, Context, EngineException }

class _mylinks(breedName: String) extends Reporter {

  def this() = this(null)

  override def syntax =
    Syntax.reporterSyntax(Syntax.LinksetType, "-T--")

  override def toString =
    super.toString + ":" + breedName

  override def report(context: Context): AnyRef = {
    val breed =
      if (breedName == null) world.links
      else world.getLinkBreed(breedName)
    for(err <- LinkManager.mustNotBeDirected(breed))
      throw new EngineException(context, this, err)
    AgentSet.fromIterator(api.AgentKind.Link,
      world.linkManager.findLinksWith(
        context.agent.asInstanceOf[Turtle], breed))
  }

}
