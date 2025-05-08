// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.Turtle
import org.nlogo.api.AgentException
import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.nvm.RuntimePrimitiveException

class _turtlevariabledouble(private var _vn: Int) extends Reporter {

  def this() = this(0)

  override def toString =
    super.toString + ":" +
      Option(world).map(_.turtlesOwnNameAt(vn)).getOrElse(vn.toString)

  // MethodRipper won't let us call a public method from report_1()
  // so we must keep vn and _vn separate - ST 9/22/12
  def vn = _vn
  def vn_=(vn: Int): Unit = { _vn = vn }

  override def report(context: Context): java.lang.Double =
    report_2(context)

  def report_1(context: Context): Double =
    context.agent.asInstanceOf[Turtle].getTurtleVariableDouble(_vn)

  def report_2(context: Context): java.lang.Double =
    try context.agent.getTurtleVariable(_vn).asInstanceOf[java.lang.Double]
    catch { case ex: AgentException =>
      throw new RuntimePrimitiveException(context, this, ex.getMessage) }

}
