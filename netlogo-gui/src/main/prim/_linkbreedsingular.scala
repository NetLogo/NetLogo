// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ AgentSet, Link }
import org.nlogo.api.{ LogoException, Syntax }
import org.nlogo.core.Nobody
import org.nlogo.nvm.{ Context, Reporter }

class _linkbreedsingular(breedName: String) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      Array[Int](Syntax.NumberType, Syntax.NumberType),
      Syntax.LinkType | Syntax.NobodyType)

  override def toString: String = s"${super.toString}:$breedName"

  override def report(context: Context): AnyRef = {
    val breed = world.getLinkBreed(breedName)
    val link = world.getLink(argEvalDouble(context, 0), argEvalDouble(context, 1), breed)
    if (link == null) Nobody else link
  }
}
