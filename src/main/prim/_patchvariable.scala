// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, AgentException, AgentKind }
import org.nlogo.nvm.{ Reporter, Context, EngineException, Reference, Referenceable }
import org.nlogo.agent.Patch

class _patchvariable(_vn: Int) extends Reporter with Referenceable {

  override def syntax =
    Syntax.reporterSyntax(
      Syntax.WildcardType | Syntax.ReferenceType, "-TP-")

  override def toString =
    super.toString + ":" +
      Option(world).map(_.patchesOwnNameAt(vn)).getOrElse(vn.toString)

  def makeReference =
    new Reference(AgentKind.Patch, vn, this)

  // MethodRipper won't let us call a public method from report_1()
  // so we must keep vn and _vn separate - ST 9/22/12
  def vn = _vn

  override def report(context: Context): AnyRef =
    report_1(context)

  def report_1(context: Context): AnyRef =
    try context.agent.getPatchVariable(_vn)
    catch { case ex: AgentException =>
      throw new EngineException(context, this, ex.getMessage) }

}
