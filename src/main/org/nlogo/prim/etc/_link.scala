// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Link
import org.nlogo.api.{ Syntax, Nobody }
import org.nlogo.nvm.{ Reporter, Context }

class _link extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.NumberType, Syntax.NumberType),
      Syntax.LinkType | Syntax.NobodyType)

  override def report(context: Context): AnyRef = {
    val link =
      world.getLink(
        argEvalDouble(context, 0),
        argEvalDouble(context, 1), world.links)
    if (link == null) Nobody
    else link
  }

}
