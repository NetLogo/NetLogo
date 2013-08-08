// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Reporter, Pure, Context }

class _constdouble(value: java.lang.Double) extends Reporter with Pure {

  private[this] val _primitiveValue = value.doubleValue
  def primitiveValue = _primitiveValue

  override def syntax =
    Syntax.reporterSyntax(Syntax.NumberType)

  override def toString =
    super.toString + ":" + primitiveValue

  override def report(context: Context): java.lang.Double =
    report_1(context)

  def report_1(context: Context): java.lang.Double =
    value

  def report_2(context: Context): Double =
    _primitiveValue

}
