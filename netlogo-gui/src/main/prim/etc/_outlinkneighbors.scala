// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.{ AgentSet, LinkManager, Turtle }
import org.nlogo.core.AgentKind
import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.nvm.RuntimePrimitiveException

class _outlinkneighbors(val breedName: String) extends Reporter {

  def this() = this(null)

  override def report(context: Context): AgentSet = {
    val breed =
      if (breedName == null)
        world.links
      else
        world.getLinkBreed(breedName)
    for(err <- LinkManager.mustNotBeUndirected(breed))
      throw new RuntimePrimitiveException(context, this, err)
    AgentSet.fromIterable(AgentKind.Turtle,
      world.linkManager.findLinkedFrom(
        context.agent.asInstanceOf[Turtle], breed))
  }

}
