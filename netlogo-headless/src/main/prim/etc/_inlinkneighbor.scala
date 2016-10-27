// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.{AgentSet, Turtle}
import org.nlogo.nvm.Context
import org.nlogo.nvm.Reporter

class _inlinkneighbor(private[this] val breedName: String) extends Reporter {
  def this() = this(null)

  override def toString: String = {
    return super.toString + ":" + breedName
  }

  def report(context: Context): AnyRef = {
    val parent: Turtle = context.agent.asInstanceOf[Turtle]
    val target: Turtle = argEvalTurtle(context, 0)
    val breed: AgentSet = if (breedName == null) world.links else world.getLinkBreed(breedName)
    return Boolean.box(world.linkManager.linksTo(target, parent, breed).length > 0)
  }
}
