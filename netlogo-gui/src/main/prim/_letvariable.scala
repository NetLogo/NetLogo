// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.{ Let, Syntax }
import org.nlogo.nvm.{ Context, Reporter }

class _letvariable(private[this] val _let: Let) extends Reporter {
  val let: Let = _let

  override def toString(): String = s"${super.toString}(${let.name})"

  override def report(context: Context): AnyRef = report_1(context)

  def report_1(context: Context): AnyRef = context.activation.binding.getLet(_let)
}

object _letvariable {
  def unapply(l: _letvariable): Option[Let] =
    Some(l.let)
}
