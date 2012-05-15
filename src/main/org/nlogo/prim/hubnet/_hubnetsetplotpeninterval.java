// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.hubnet;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;

public final strictfp class _hubnetsetplotpeninterval
    extends org.nlogo.nvm.Command {
  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    final String name = argEvalString(context, 0);
    final double interval = argEvalDoubleValue(context, 1);
    workspace.waitFor(new org.nlogo.api.CommandRunnable() {
      public void run() {
        workspace.getHubNetManager().setPlotPenInterval(name, interval);
      }
    });
    context.ip = next;
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.StringType(), Syntax.NumberType()};
    return Syntax.commandSyntax(right);
  }
}
