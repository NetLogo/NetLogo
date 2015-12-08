// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ AgentException, LogoException, Syntax }
import org.nlogo.nvm.{ Context, EngineException, Reporter }

class _linkbreedvariable(private[this] val _name: String) extends Reporter {
  override def syntax: Syntax =
    Syntax.reporterSyntax(Syntax.WildcardType | Syntax.ReferenceType, "---L")

  override def toString: String = s"${super.toString}:$name"

  override def report(context: Context): AnyRef =
    try {
      context.agent.getLinkBreedVariable(_name)
    } catch {
      case ex: AgentException => throw new EngineException(context, this, ex.getMessage)
    }

  def report_1(context: Context): AnyRef =
    try {
      context.agent.getLinkBreedVariable(_name)
    } catch {
      case ex: AgentException => throw new EngineException(context, this, ex.getMessage)
    }

  def name = _name
}
