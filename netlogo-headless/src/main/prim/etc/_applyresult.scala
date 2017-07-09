// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ AnonymousProcedure, Context, Reporter, RuntimePrimitiveException }

class _applyresult extends Reporter {
  override def report(context: Context) = {
    val rep = argEvalAnonymousReporter(context, 0)
    val list = argEvalList(context, 1)
    if (list.size < rep.syntax.minimum)
      throw new RuntimePrimitiveException(context, this, AnonymousProcedure.missingInputs(rep, list.size))
    rep.report(context, list.toVector.toArray)
  }
}
