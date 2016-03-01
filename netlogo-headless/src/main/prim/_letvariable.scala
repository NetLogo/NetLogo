// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Let
import org.nlogo.nvm.{ Context, Reporter }

class _letvariable(private[this] val _let: Let) extends Reporter {

  def this() = this(null)

  // MethodRipper won't let us call a public method from report_1() - ST 7/20/12
  def let: Let = _let

  override def toString =
    s"${super.toString}($let)"

  override def report(context: Context): AnyRef =
    report_1(context)

  def report_1(context: Context): AnyRef =
    context.getLet(_let)
}
