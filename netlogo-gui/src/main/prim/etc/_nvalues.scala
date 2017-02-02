// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ I18N, LogoList }
import org.nlogo.api.{ LogoListBuilder}
import org.nlogo.core.Syntax
import org.nlogo.nvm.{ AnonymousProcedure, Context, Reporter }
import org.nlogo.nvm.RuntimePrimitiveException

class _nvalues extends Reporter {

  override def report(context: Context) = {
    // get the first argument...
    val n = argEvalIntValue(context, 0)
    if (n < 0)
      throw new RuntimePrimitiveException( context, this,
        I18N.errors.getN("org.nlogo.prim.etc.$common.noNegativeNumber", displayName))
    // make the result list.
    val result = new LogoListBuilder
    val rep = argEvalAnonymousReporter(context, 1)
    if (rep.syntax.minimum > 1)
      throw new RuntimePrimitiveException(context, this, AnonymousProcedure.missingInputs(rep, 1))
    for (i <- 0 until n)
      result.add(rep.report(context, Array[AnyRef](Double.box(i))))
    result.toLogoList
  }

}
