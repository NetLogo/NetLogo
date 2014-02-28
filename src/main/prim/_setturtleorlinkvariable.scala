// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, AgentException }
import org.nlogo.nvm.{ Command, Context, EngineException }

class _setturtleorlinkvariable(_varName: String) extends Command {

  def this(original: _turtleorlinkvariable) = this(original.varName)

  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.WildcardType), "-T-L", true)

  override def toString =
    super.toString + ":" + varName

  // MethodRipper won't let us call a public method from report_1()
  // so we must keep varName and _varName separate - ST 9/22/12
  def varName = _varName

  override def perform(context: Context) {
    perform_1(context, args(0).report(context))
  }

  def perform_1(context: Context, value: AnyRef) {
    try context.agent.setTurtleOrLinkVariable(_varName, value)
    catch { case ex: AgentException =>
      throw new EngineException(context, this, ex.getMessage) }
    context.ip = next
  }

}
