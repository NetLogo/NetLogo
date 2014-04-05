// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.api.Nobody
import org.nlogo.agent.Link
import org.nlogo.nvm.{ Reporter, Context }

class _link extends Reporter {

  override def syntax =
    SyntaxJ.reporterSyntax(
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
