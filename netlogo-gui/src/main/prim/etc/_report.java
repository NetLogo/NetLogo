// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.NonLocalExit;

public final strictfp class _report extends Command {

  @Override
  public void perform(Context context) throws LogoException {
    perform_1(context, args[0].report(context));
  }

  public void perform_1(Context context, Object arg0) throws LogoException {
    context.activation.result = arg0;
    context.stopping = false;
    context.ip = next;
    if (! context.activation.nonLambdaActivation().procedure.isReporter()) {
      throw new RuntimePrimitiveException(context, this, I18N.errorsJ().getN("org.nlogo.prim._report.canOnlyUseInToReport", displayName()));
    } else if (context.activation.procedure.isLambda()) {
      throw new NonLocalExit();
    } else if (!context.atTopActivation()) {
      // you can't report from inside an ask.  you can't write code like
      //   to-report foo ask turtle 0 [ report 5 ] end
      // maybe you should be able to, but at least for now, you can't
      throw new RuntimePrimitiveException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim._report.mustImmediatelyBeUsedInToReport", displayName()));
    }
  }
}
