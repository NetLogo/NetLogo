// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.{AgentSet, Turtle}
import org.nlogo.nvm.{Context, Reporter}

class _inlinkneighbor(breedName: String) extends Reporter {
  def this() = this(null)

  override def toString: String = s"${super.toString}:$breedName"

  override def report(context: Context) = Boolean.box(report_1(context, argEvalTurtle(context, 0)))

  def report_1(context: Context, target: Turtle): Boolean = {
    val parent: Turtle = context.agent.asInstanceOf[Turtle]
    val breed: AgentSet = if (breedName == null) world.links else world.getLinkBreed(breedName)
    world.linkManager.isLinkedTo(target, parent, breed)
  }
}
