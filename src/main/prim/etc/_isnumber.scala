// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Context, Pure, Reporter }

class _isnumber extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType)
  override def report(context: Context): java.lang.Boolean =
    Boolean.box(
      args(0).report(context).isInstanceOf[java.lang.Double])
}
