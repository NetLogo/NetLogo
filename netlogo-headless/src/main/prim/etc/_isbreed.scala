// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.{ Link, Turtle }
import org.nlogo.nvm.{ Context, Reporter }

class _isbreed(val breedName: String) extends Reporter {

  override def toString =
    super.toString + ":" + breedName

  override def report(context: Context): java.lang.Boolean =
    Boolean.box(
      args(0).report(context) match {
        case turtle: Turtle =>
          turtle.id != -1 && (turtle.getBreed eq world.getBreed(breedName))
        case link: Link =>
          link.id != -1 && (link.getBreed eq world.getLinkBreed(breedName))
        case _ =>
          false
      })

}
