// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.{ AgentSet, Turtle }
import org.nlogo.core.AgentKind
import org.nlogo.nvm.{ Context, Reporter }

class _outlinkneighbors(val breedName: String) extends Reporter {

  def this() = this(null)

  override def report(context: Context): AgentSet = {
    val breed = if (breedName == null) world.links else world.getLinkBreed(breedName)
    AgentSet.fromArray(AgentKind.Turtle, world.linkManager.outNeighbors(context.agent.asInstanceOf[Turtle], breed))
  }

}
