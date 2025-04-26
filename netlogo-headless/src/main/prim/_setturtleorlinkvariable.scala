// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.AgentException
import org.nlogo.agent.{ Turtle, Link }
import org.nlogo.nvm.{ Command, Context, RuntimePrimitiveException }

class _setturtleorlinkvariable(_varName: String, _vnTurtle: Int, _vnLink: Int) extends Command {
  switches = true

  override def toString =
    super.toString + ":" + varName

  // MethodRipper won't let us call a public method from report_1()
  // so we must keep varName and _varName separate - ST 9/22/12
  def varName = _varName

  override def perform(context: Context): Unit = {
    perform_1(context, args(0).report(context))
  }

  def perform_1(context: Context, value: AnyRef): Unit = {
    val a = context.agent
    try {
      a.agentBit match {
        case Turtle.BIT => a.setTurtleVariable(_vnTurtle, value)
        case Link.BIT   => a.setLinkVariable(_vnLink, value)
        case _ => a.setTurtleOrLinkVariable(_varName, value)
      }
    } catch {
      case ex: AgentException =>
        throw new RuntimePrimitiveException(context, this, ex.getMessage)
    }
    context.ip = next
  }
}
