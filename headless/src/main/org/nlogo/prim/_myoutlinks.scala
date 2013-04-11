// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ AgentSet, Turtle }
import org.nlogo.api, api.Syntax
import org.nlogo.nvm.{ Reporter, Context }

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
    mustNotBeUndirected(breed, context)
    AgentSet.fromIterator(api.AgentKind.Link,
      world.linkManager.findLinksFrom(
        context.agent.asInstanceOf[Turtle], breed))
  }

}
