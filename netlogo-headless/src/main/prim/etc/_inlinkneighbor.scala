// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.{ LinkManager, Turtle }
import org.nlogo.nvm.{ Context, EngineException, Reporter }

class _inlinkneighbor(val breedName: String) extends Reporter {

  def this() = this(null)

  override def toString =
    super.toString + ":" + breedName

  override def report(context: Context): java.lang.Boolean = {
    val target = argEvalTurtle(context, 0)
    val breed =
      if (breedName == null)
        world.links
      else
        world.getLinkBreed(breedName)
    if (breed.isUndirected)
      Boolean.box(false)
    else {
      val link =
        world.linkManager.findLinkFrom(target, context.agent.asInstanceOf[Turtle], breed, true)
      if (link == null || link.getBreed.isUndirected) Boolean.box(false)
      else Boolean.box(true)
    }
  }

}
