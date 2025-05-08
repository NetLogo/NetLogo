// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.AgentException
import org.nlogo.nvm.{ Context, Reporter, RuntimePrimitiveException }

class _linkvariable(private val _vn: Int) extends Reporter {


  override def toString =
      s"${super.toString()}:${if (world != null) world.linksOwnNameAt(vn) else vn}"

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
