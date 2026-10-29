// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.AgentException
import org.nlogo.core.{ AgentKind, Reference, Referenceable }
import org.nlogo.nvm.{ Context, Reporter, RuntimePrimitiveException }

class _linkvariable(_vn: Int) extends Reporter with Referenceable {

  override def toString =
      s"${super.toString()}:${if (world != null) world.linksOwnNameAt(vn) else vn}"

  def makeReference: Reference =
    new Reference(AgentKind.Link, vn, this)

  override def report(context: Context): AnyRef =
    try {
      context.agent.getLinkVariable(_vn)
    } catch {
      case ex: AgentException => throw new RuntimePrimitiveException(context, this, ex.getMessage)
    }

  def report_1(context: Context): AnyRef =
    try {
      context.agent.getLinkVariable(_vn)
    } catch {
      case ex: AgentException => throw new RuntimePrimitiveException(context, this, ex.getMessage)
    }

  def vn = _vn
}
