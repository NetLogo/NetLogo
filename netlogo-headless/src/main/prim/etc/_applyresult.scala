// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ LogoException, AnonymousReporter }
import org.nlogo.core.{ I18N, Syntax }
import org.nlogo.core.CompilerException
import org.nlogo.nvm.{ Activation, AnonymousProcedure, ArgumentTypeException, Context, Reporter }
import org.nlogo.nvm.RuntimePrimitiveException

class _applyresult extends Reporter {
  override def report(context: Context) = {
    val rep = argEvalAnonymousReporter(context, 0)
    val list = argEvalList(context, 1)
    if (list.size < rep.syntax.minimum)
      throw new RuntimePrimitiveException(context, this, AnonymousProcedure.missingInputs(rep, list.size))
    rep.report(context, list.toVector.toArray)
  }
}
