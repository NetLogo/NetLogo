// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.{ LinkManager, Turtle }
import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.nvm.RuntimePrimitiveException

class _linkneighbor(breedName: String) extends Reporter {
  def this() = this(null)

  override def toString = s"${super.toString}:$breedName"

  override def report(context: Context) = Boolean.box(report_1(context, argEvalTurtle(context, 0)))

  def report_1(context: Context, target: Turtle): Boolean = {
    val parent = context.agent.asInstanceOf[Turtle]
    val breed = if (breedName == null) world.links else world.getLinkBreed(breedName)
    val linkManager = world.linkManager
    linkManager.linksWith(parent, target, breed).nonEmpty
  }
}
