// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, AgentException }
import org.nlogo.nvm.{ Reporter, Context, EngineException }
import org.nlogo.agent.{ Patch, Turtle }

class _patchvariabledouble(private[this] var _vn: Int) extends Reporter {

  def this() = this(0)

  override def syntax =
    Syntax.reporterSyntax(Syntax.NumberType, "-TP-")

  override def toString =
    super.toString + ":" +
      Option(world).map(_.patchesOwnNameAt(vn)).getOrElse(vn.toString)

  // MethodRipper won't let us call a public method from report_1()
  // so we must keep vn and _vn separate - ST 9/22/12
  def vn = _vn
  def vn_=(vn: Int) { _vn = vn }

  override def report(context: Context): java.lang.Double =
    report_1(context)

  def report_1(context: Context): java.lang.Double =
    try context.agent.getPatchVariable(_vn).asInstanceOf[java.lang.Double]
    catch { case ex: AgentException =>
      throw new EngineException(context, this, ex.getMessage) }

  def report_2(context: Context): Double = {
    val patch = context.agent match {
      case t: Turtle => t.getPatchHere
      case p: Patch => p
    }
    patch.getPatchVariableDouble(_vn)
  }

}
