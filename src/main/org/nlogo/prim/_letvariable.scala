// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, Let }
import org.nlogo.nvm.{ Context, Reporter }

class _letvariable(_let: Let) extends Reporter {

  // MethodRipper won't let us call a public method from perform_1() - ST 7/20/12
  def let = _let

  override def syntax =
    Syntax.reporterSyntax(Syntax.WildcardType)
  override def toString =
    super.toString + "(" + _let.name + ")"

  override def report(context: Context) =
    report_1(context)
  def report_1(context: Context) =
    context.getLet(_let)

}
