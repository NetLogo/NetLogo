// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ AgentSet, LinkManager, Turtle }
import org.nlogo.api.{ LogoException, Syntax }
import org.nlogo.nvm.{ Context, Reporter }

class _linkneighbor(breedName: String) extends Reporter {
  def this() = this(null)

  override def syntax: Syntax =
    Syntax.reporterSyntax(
      Array[Int](Syntax.AgentType),
      Syntax.BooleanType, "-T--")

  override def toString = s"${super.toString}:$breedName"

  override def report(context: Context) = report_1(context, argEvalTurtle(context, 0)).asInstanceOf[AnyRef]

  def report_1(context: Context, target: Turtle): Boolean = {
    val parent = context.agent.asInstanceOf[Turtle]
    val breed = if (breedName == null) world.links else world.getLinkBreed(breedName)
    mustNotBeDirected(breed, context)
    val linkManager = world.linkManager
    linkManager.findLinkFrom(parent, target, breed, true) != null ||
      linkManager.findLinkFrom(target, parent, breed, true) != null
  }
}
