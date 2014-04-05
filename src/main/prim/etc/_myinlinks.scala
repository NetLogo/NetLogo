// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ, AgentKind }
import org.nlogo.agent.{ AgentSet, Turtle, LinkManager }
import org.nlogo.nvm.{ Reporter, Context, EngineException }

class _myinlinks(breedName: String) extends Reporter {

  def this() = this(null)

  override def syntax =
    SyntaxJ.reporterSyntax(Syntax.LinksetType, "-T--")

  override def toString =
    super.toString + ":" + breedName

  override def report(context: Context): AgentSet = {
    val breed =
      if (breedName == null) world.links
      else world.getLinkBreed(breedName)
    for(err <- LinkManager.mustNotBeUndirected(breed))
      throw new EngineException(context, this, err)
    AgentSet.fromIterator(AgentKind.Link,
      world.linkManager.findLinksTo(
        context.agent.asInstanceOf[Turtle], breed))
  }

}
