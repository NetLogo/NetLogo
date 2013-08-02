// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, AgentException }
import org.nlogo.nvm.{ Command, Context, EngineException }

class _setlinkvariable(_vn: Int) extends Command {

  def this(original: _linkvariable) = this(original.vn)

  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.WildcardType), "---L", true)

  override def toString =
    super.toString + ":" +
      Option(world).map(_.linksOwnNameAt(vn)).getOrElse(vn.toString)

  // MethodRipper won't let us call a public method from report_1()
  // so we must keep vn and _vn separate - ST 9/22/12
  def vn = _vn

  override def perform(context: Context) {
    perform_1(context, args(0).report(context))
  }

  def perform_1(context: Context, value: AnyRef) {
    try context.agent.setLinkVariable(_vn, value)
    catch { case ex: AgentException =>
      throw new EngineException(context, this, ex.getMessage) }
    context.ip = next
  }

}
