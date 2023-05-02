// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Turtle
import org.nlogo.nvm.{Context, Reporter}

class _linkneighbor(breedName: String) extends Reporter {
  def this() = this(null)

  override def toString = s"${super.toString}:$breedName"

  override def report(context: Context) = Boolean.box(report_1(context, argEvalTurtle(context, 0)))

  def report_1(context: Context, target: Turtle): Boolean = {
    val parent = context.agent.asInstanceOf[Turtle]
    val breed = if (breedName == null) world.links else world.getLinkBreed(breedName)
    world.linkManager.isLinkedWith(parent, target, breed)
  }
}
