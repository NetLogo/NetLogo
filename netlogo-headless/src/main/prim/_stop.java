// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.core.I18N;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.RuntimePrimitiveException;

public final class _stop
    extends Command {

  @Override
  public void perform(final org.nlogo.nvm.Context context) {
    perform_1(context);
  }

  public void perform_1(final org.nlogo.nvm.Context context) {
    // check: are we in an ask?
    if (!context.atTopActivation()) {
      // if so, then "stop" means that this agent prematurely
      // finishes its participation in the ask.
      context.finished = true;
    } else if (context.activation.nonLambdaActivation().procedure().isReporter()) {
      // if we're not in an ask, then "stop" means to exit this procedure
      // immediately.  first we must check that it's a command procedure
      // and not a reporter procedure.
      throw new RuntimePrimitiveException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc._stop.notAllowedInsideToReport", displayName()));
    }
    context.stop();
  }

  // identical to perform_1() above... BUT with a profiling hook added
  public void profiling_perform_1(final org.nlogo.nvm.Context context) {
    if (!context.atTopActivation()) {
      context.finished = true;
    } else if (context.activation.nonLambdaActivation().procedure().isReporter()) {
      throw new RuntimePrimitiveException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc._stop.notAllowedInsideToReport", displayName()));
    }
    workspace.profilingTracer().closeCallRecord(context, context.activation);
    context.stop();
  }
}
