// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.agent.Link
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _linkpitch extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      Syntax.NumberType, "---L")
  override def report(context: Context) = {
    val link = context.agent.asInstanceOf[Link]
    try Double.box(world.protractor.towardsPitch(link.end1, link.end2, true))
    catch {
      case e: org.nlogo.api.AgentException =>
        throw new org.nlogo.nvm.EngineException(
          context, this,
          "there is no pitch of a link whose endpoints are in the same position")
    }
  }
}
