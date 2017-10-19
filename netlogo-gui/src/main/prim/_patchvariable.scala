// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.AgentException
import org.nlogo.core.{ AgentKind, Reference, Referenceable }
import org.nlogo.nvm.{ Context, Reporter, RuntimePrimitiveException }

class _patchvariable(private[this] val _vn: Int) extends Reporter with Referenceable {
  override def toString: String =
    s"${super.toString}:${if (world == null) vn else world.patchesOwnNameAt(vn)}"

  def makeReference: Reference = return new Reference(AgentKind.Patch, vn, this)

  override def report(context: Context) = report_1(context)

  def report_1(context: Context): AnyRef =
    try {
      context.agent.getPatchVariable(_vn)
    } catch {
      case ex: AgentException => throw new RuntimePrimitiveException(context, this, ex.getMessage)
    }

  def vn = _vn
}
