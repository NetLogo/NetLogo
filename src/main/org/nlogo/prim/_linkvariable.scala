// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ AgentException, LogoException, Syntax }
import org.nlogo.nvm.{ Context, EngineException, Reporter }

class _linkvariable(private[this] val _vn: Int) extends Reporter {
  override def syntax: Syntax =
    Syntax.reporterSyntax(Syntax.WildcardType | Syntax.ReferenceType, "---L")

  override def toString =
      s"${super.toString()}:${if (world != null) world.linksOwnNameAt(vn) else vn}"

  override def report(context: Context): AnyRef =
    try {
      context.agent.getLinkVariable(_vn)
    } catch {
      case ex: AgentException => throw new EngineException(context, this, ex.getMessage)
    }

  def report_1(context: Context): AnyRef =
    try {
      context.agent.getLinkVariable(_vn)
    } catch {
      case ex: AgentException => throw new EngineException(context, this, ex.getMessage)
    }

  def vn = _vn
}
