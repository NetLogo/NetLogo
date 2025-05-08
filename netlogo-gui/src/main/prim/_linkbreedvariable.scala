// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.AgentException
import org.nlogo.nvm.{ Context, Reporter, RuntimePrimitiveException }

class _linkbreedvariable(private val _name: String) extends Reporter {


  override def toString: String = s"${super.toString}:$name"

  override def report(context: Context): AnyRef =
    try {
      context.agent.getLinkBreedVariable(_name)
    } catch {
      case ex: AgentException => throw new RuntimePrimitiveException(context, this, ex.getMessage)
    }

  def report_1(context: Context): AnyRef =
    try {
      context.agent.getLinkBreedVariable(_name)
    } catch {
      case ex: AgentException => throw new RuntimePrimitiveException(context, this, ex.getMessage)
    }

  def name = _name
}
