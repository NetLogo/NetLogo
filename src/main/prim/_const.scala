// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

// Not currently employed by the compiler, but may be useful in some scenarios, such as for users of
// the controlling API.  (See TestArgumentInjection for a sample use.)

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Reporter, Pure, Context }

class _const(value: AnyRef) extends Reporter with Pure {

  override def syntax =
    Syntax.reporterSyntax(Syntax.WildcardType)

  override def toString =
    s"${super.toString}:$value"

  override def report(context: Context): AnyRef =
    report_1(context)

  def report_1(context: Context): AnyRef =
    value

}
