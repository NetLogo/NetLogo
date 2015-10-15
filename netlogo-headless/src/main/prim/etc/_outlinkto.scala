// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.{ LinkManager, Turtle }
import org.nlogo.core.Nobody
import org.nlogo.nvm.{ Context, EngineException, Reporter }

class _outlinkto(val breedName: String) extends Reporter {

  def this() = this(null)

  override def toString =
    super.toString + ":" + breedName

  override def report(context: Context): AnyRef = {
    val parent = context.agent.asInstanceOf[Turtle]
    val target = argEvalTurtle(context, 0)
    val breed =
      if (breedName == null) world.links
      else world.getLinkBreed(breedName)
    for(err <- LinkManager.mustNotBeUndirected(breed))
      throw new EngineException(context, this, err)
    val link = world.linkManager.findLinkFrom(parent, target, breed, true)
    if (link == null)
      Nobody
    else
      link
  }

}
