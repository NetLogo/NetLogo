// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, AgentKind }
import org.nlogo.nvm.{ Reporter, Context, Reference }
import org.nlogo.agent.Observer

class _observervariable(_vn: Int) extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Syntax.WildcardType | Syntax.ReferenceType)

  override def toString =
    super.toString + ":" +
      Option(world).map(_.observerOwnsNameAt(vn)).getOrElse(vn.toString)

  // MethodRipper won't let us call a public method from report_1()
  // so we must keep vn and _vn separate - ST 9/22/12
  def vn = _vn

  def makeReference =
    new Reference(AgentKind.Observer, _vn, this)

  override def report(context: Context) =
    context.agent.getObserverVariable(_vn)

  def report_1(context: Context) =
    context.agent.getObserverVariable(_vn)

}
