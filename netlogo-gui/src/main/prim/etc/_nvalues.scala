// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.LogoListBuilder
import org.nlogo.core.LocaleContext
import org.nlogo.nvm.{ AnonymousProcedure, Context, Reporter, RuntimePrimitiveException }

class _nvalues extends Reporter {

  override def report(context: Context) = {
    // get the first argument...
    val n = argEvalIntValue(context, 0)
    if (n < 0)
      throw new RuntimePrimitiveException(context, this, loc"$displayName cannot take a negative number.")
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
