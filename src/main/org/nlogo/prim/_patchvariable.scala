// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.Patch
import org.nlogo.api.{ AgentException, LogoException, Syntax }
import org.nlogo.nvm.{ Context, EngineException, Reference, Referenceable, Reporter }

class _patchvariable(private[this] val _vn: Int) extends Reporter with Referenceable {
  override def syntax: Syntax =
    Syntax.reporterSyntax(
      Syntax.WildcardType | Syntax.ReferenceType,
      "-TP-")

  override def toString: String =
    s"${super.toString}:${if (world == null) vn else world.patchesOwnNameAt(vn)}"

  def makeReference: Reference = new Reference(classOf[Patch], vn, this)

  override def report(context: Context) = report_1(context)

  def report_1(context: Context): AnyRef =
    try {
      context.agent.getPatchVariable(_vn)
    } catch {
      case ex: AgentException => throw new EngineException(context, this, ex.getMessage)
    }

  def vn = _vn
}
