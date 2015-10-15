// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.{ AgentSet, LinkManager, Turtle }
import org.nlogo.core.AgentKind
import org.nlogo.nvm.{ Context, EngineException, Reporter }

class _linkneighbors(breedName: String) extends Reporter {

  def this() = this(null)

  override def toString =
    super.toString + ":" + breedName

  override def report(context: Context): AgentSet =
    report_1(context)

  def report_1(context: Context): AgentSet = {
    val breed =
      if (breedName == null)
        world.links
      else
        world.getLinkBreed(breedName)
    val err = LinkManager.mustNotBeDirected(breed)
    if (err.isDefined)
      throw new EngineException(context, this, err.get)
    AgentSet.fromIterator(AgentKind.Turtle,
      world.linkManager.findLinkedWith(
        context.agent.asInstanceOf[Turtle], breed))
  }

  def getBreedName = breedName

}
