// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ AgentException}
import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.nvm.RuntimePrimitiveException

class _breedvariable(private[this] val _name: String) extends Reporter {

  override def toString: String = s"${super.toString}:$name"

  override def report(context: Context) = report_1(context)

  def report_1(context: Context): AnyRef =
    try {
      context.agent.getBreedVariable(_name)
    } catch {
      case ex: AgentException => throw new RuntimePrimitiveException(context, this, ex.getMessage)
    }

  def name = _name
}
