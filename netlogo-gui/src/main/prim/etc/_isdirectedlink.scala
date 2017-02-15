// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Link
import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.core.Pure

class _isdirectedlink extends Reporter with Pure {

  override def report(context: Context) =
    Boolean.box(
      args(0).report(context) match {
        case link: Link =>
          link.getBreed.isDirected
        case _ =>
          false
      })
}
