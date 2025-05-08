// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Patch, Turtle }
import org.nlogo.api.AgentException
import org.nlogo.nvm.{ Context, Reporter, RuntimePrimitiveException }

class _patchvariabledouble extends Reporter {
  private var _vn: Int = 0
  def vn = _vn
  def vn_=(new_vn: Int) = _vn = new_vn



  override def toString: String =
    s"${super.toString}:${if (world == null) vn else world.patchesOwnNameAt(vn)}"

  override def report(context: Context) = report_1(context)

  def report_1(context: Context): java.lang.Double =
    try {
      context.agent.getPatchVariable(_vn).asInstanceOf[Double]
    } catch {
      case ex: AgentException => throw new RuntimePrimitiveException(context, this, ex.getMessage)
    }

  def report_2(context: Context): Double = {
    val patch = context.agent match {
      case turtle: Turtle => turtle.getPatchHere
      case agent => agent.asInstanceOf[Patch]
    }
    patch.getPatchVariableDouble(_vn)
  }
}
