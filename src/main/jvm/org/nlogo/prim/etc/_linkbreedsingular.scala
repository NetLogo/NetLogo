// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Syntax, Nobody }
import org.nlogo.nvm.{ Reporter, Context }
import org.nlogo.agent.{ AgentSet, Link }

class _linkbreedsingular(breedName: String) extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.NumberType, Syntax.NumberType),
      Syntax.LinkType | Syntax.NobodyType)

  override def toString =
    super.toString + ":" + breedName

  override def report(context: Context) = {
    val breed = world.getLinkBreed(breedName)
    val link = world.getLink(
      argEvalDouble(context, 0), argEvalDouble(context, 1), breed)
    if (link == null)
      Nobody
    else
      link
  }

}
