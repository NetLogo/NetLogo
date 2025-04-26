// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.AgentException
import org.nlogo.nvm.{ Command, Context}
import org.nlogo.nvm.RuntimePrimitiveException

class _setlinkvariable(_vn: Int) extends Command {

  def this(original: _linkvariable) = this(original.vn)

  switches = true

  override def toString =
    super.toString + ":" +
      Option(world).map(_.linksOwnNameAt(vn)).getOrElse(vn.toString)

  // MethodRipper won't let us call a public method from report_1()
  // so we must keep vn and _vn separate - ST 9/22/12
  def vn = _vn

  override def perform(context: Context): Unit = {
    perform_1(context, args(0).report(context))
  }

  def perform_1(context: Context, value: AnyRef): Unit = {
    try context.agent.setLinkVariable(_vn, value)
    catch { case ex: AgentException =>
      throw new RuntimePrimitiveException(context, this, ex.getMessage) }
    context.ip = next
  }

}
