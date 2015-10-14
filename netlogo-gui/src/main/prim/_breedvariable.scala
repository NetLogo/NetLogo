// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ AgentException, Syntax }
import org.nlogo.nvm.{ Context, EngineException, Reporter }

class _breedvariable(private[this] val _name: String) extends Reporter {
  override def syntax: Syntax =
    Syntax.reporterSyntax(Syntax.WildcardType | Syntax.ReferenceType, "-T--")

  override def toString: String = s"${super.toString}:$name"

  override def report(context: Context) = report_1(context)

  def report_1(context: Context): AnyRef =
    try {
      context.agent.getBreedVariable(_name)
    } catch {
      case ex: AgentException => throw new EngineException(context, this, ex.getMessage)
    }

  def name = _name
}
