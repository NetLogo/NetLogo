// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, AgentException }
import org.nlogo.nvm.{ Reporter, Context, EngineException }

class _linkvariable(_vn: Int) extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Syntax.WildcardType | Syntax.ReferenceType, "---L")

  override def toString =
    super.toString + ":" +
      Option(world).map(_.linksOwnNameAt(vn)).getOrElse(vn.toString)

  // MethodRipper won't let us call a public method from report_1()
  // so we must keep vn and _vn separate - ST 9/22/12
  def vn = _vn

  override def report(context: Context) =
    report_1(context)

  def report_1(context: Context) =
    try context.agent.getLinkVariable(_vn)
    catch { case ex: AgentException =>
      throw new EngineException(context, this, ex.getMessage) }

}
