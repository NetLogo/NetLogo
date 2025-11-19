// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import java.lang.{ Double => JDouble }

import org.nlogo.core.Pure
import org.nlogo.nvm.{ Context, Reporter }

class _constdouble(_primitiveValue: Double) extends Reporter with Pure {

  def this(value: JDouble) = this(value: Double)

  private val value = Double.box(_primitiveValue)
  val primitiveValue = _primitiveValue

  override def toString =
    super.toString + ":" + _primitiveValue

  override def report(context: Context): java.lang.Double =
    report_1(context)

  def report_1(context: Context): java.lang.Double =
    value

  def report_2(context: Context): Double =
    _primitiveValue

}
