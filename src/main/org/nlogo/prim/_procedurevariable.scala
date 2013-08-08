// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _procedurevariable(_vn: Int, val name: String) extends Reporter {
  // MethodRipper won't let us call public methods from report_1() - ST 8/8/12
  val vn = _vn
  override def syntax =
    Syntax.reporterSyntax(Syntax.WildcardType)
  override def toString =
    super.toString + ":" + name
  override def report(context: Context): AnyRef =
    report_1(context)
  def report_1(context: Context): AnyRef =
    context.activation.args(_vn)
}
