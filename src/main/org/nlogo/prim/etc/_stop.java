// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Procedure;

public final strictfp class _stop
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax();
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    perform_1(context);
  }

  public void perform_1(final org.nlogo.nvm.Context context) throws LogoException {
    // check: are we in an ask?
    if (!context.atTopActivation()) {
      // if so, then "stop" means that this agent prematurely
      // finishes its participation in the ask.
      context.finished = true;
    } else {
      // if we're not in an ask, then "stop" means to exit this procedure
      // immediately.  first we must check that it's a command procedure
      // and not a reporter procedure.
      if (context.activation.procedure().tyype == Procedure.Type.REPORTER ||
          context.activation.procedure().isTask() && context.activation.procedure().parent.tyype == Procedure.Type.REPORTER) {
        throw new EngineException(context, this,
            I18N.errorsJ().getN("org.nlogo.prim.etc._stop.notAllowedInsideToReport", displayName()));
      }
      context.stop();
    }
  }

  // identical to perform_1() above... BUT with a profiling hook added
  public void profiling_perform_1(final org.nlogo.nvm.Context context) throws LogoException {
    if (!context.atTopActivation()) {
      context.finished = true;
    } else {
      if (context.activation.procedure().tyype == Procedure.Type.REPORTER ||
          context.activation.procedure().isTask() && context.activation.procedure().parent.tyype == Procedure.Type.REPORTER) {
        throw new EngineException(context, this,
            I18N.errorsJ().getN("org.nlogo.prim.etc._stop.notAllowedInsideToReport", displayName()));
      }
      workspace.profilingTracer().closeCallRecord(context, context.activation);
      context.stop();
    }
  }
}
