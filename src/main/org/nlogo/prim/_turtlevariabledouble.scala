// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, AgentException }
import org.nlogo.nvm.{ Reporter, Context, EngineException }
import org.nlogo.agent.Turtle

class _turtlevariabledouble(private[this] var _vn: Int) extends Reporter {

  def this() = this(0)

  override def syntax =
    Syntax.reporterSyntax(Syntax.NumberType, "-T--")

  override def toString =
    super.toString + ":" +
      Option(world).map(_.turtlesOwnNameAt(vn)).getOrElse(vn.toString)

  // MethodRipper won't let us call a public method from report_1()
  // so we must keep vn and _vn separate - ST 9/22/12
  def vn = _vn
  def vn_=(vn: Int) { _vn = vn }

  override def report(context: Context) =
    report_2(context)

  def report_1(context: Context): Double =
    context.agent.asInstanceOf[Turtle].getTurtleVariableDouble(_vn)

  def report_2(context: Context): java.lang.Double =
    try context.agent.getTurtleVariable(_vn).asInstanceOf[java.lang.Double]
    catch { case ex: AgentException =>
      throw new EngineException(context, this, ex.getMessage) }

}
