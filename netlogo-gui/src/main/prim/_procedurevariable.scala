// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.nvm.{ Context, Reporter }

class _procedurevariable(private val _vn: Int, val name: String) extends Reporter {
  override def toString: String = s"${super.toString}:$name"

  def vn = _vn

  override def report(context: Context): AnyRef = report_1(context)

  def report_1(context: Context): AnyRef = context.activation.args(_vn)
}

object _procedurevariable {
  def unapply(pv: _procedurevariable): Option[(Int, String)] =
    Some((pv.vn, pv.name))
}
