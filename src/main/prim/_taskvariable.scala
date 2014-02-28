// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _taskvariable(val varNumber: Int) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.WildcardType)
  override def toString =
    super.toString + ":" + varNumber
  override def report(context: Context): Nothing =
    // TaskVisitor compiles us out of existence
    throw new IllegalStateException
}
