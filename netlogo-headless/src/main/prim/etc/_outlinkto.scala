// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Turtle
import org.nlogo.core.Nobody
import org.nlogo.nvm.{ Context, Reporter }

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
    val links = world.linkManager.linksTo(parent, target, breed)
    if (links.isEmpty)
      Nobody
    else if (links.size == 1) // Avoid needlessly invoking RNG
      links.head
    else
      links(context.getRNG.nextInt(links.size))
  }

}
