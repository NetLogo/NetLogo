// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Reporter, Context }
import org.nlogo.agent.{ Turtle, Link }

class _isbreed(breedName: String) extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.WildcardType),
      Syntax.BooleanType)

  override def toString =
    super.toString + ":" + breedName

  override def report(context: Context) =
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
