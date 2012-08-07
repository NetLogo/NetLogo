// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

public final strictfp class _return extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax();
  }

  @Override
  public String displayName() {
    return "END"; // for use in error messages
  }

  @Override
  public void perform(Context context) {
    perform_1(context);
  }

  public void perform_1(Context context) {
    context.returnFromProcedure();
    // see _stop.perform() for commentary on this - ST 7/15/04
    context.stopping = false;
  }

  public void profiling_perform_1(Context context) {
    // profiling data collection, close out the call record
    workspace.profilingTracer().closeCallRecord(context, context.activation);
    context.returnFromProcedure();
    // see _stop.perform() for commentary on this - ST 7/15/04
    context.stopping = false;
  }
}
