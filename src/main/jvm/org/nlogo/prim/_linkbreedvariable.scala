// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, AgentException }
import org.nlogo.nvm.{ Reporter, Context, EngineException }

class _linkbreedvariable(_name: String) extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Syntax.WildcardType | Syntax.ReferenceType, "---L")

  override def toString =
    super.toString + ":" + name

  // MethodRipper won't let us call a public method from report_1()
  // so we must keep name and _name separate - ST 9/22/12
  def name = _name

  override def report(context: Context) =
    report_1(context)

  def report_1(context: Context) =
    try context.agent.getLinkBreedVariable(_name)
    catch { case ex: AgentException =>
      throw new EngineException(context, this, ex.getMessage) }

}
