// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.hubnet;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;

public final strictfp class _hubnetplot
    extends org.nlogo.nvm.Command {
  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    final String name = argEvalString(context, 0);
    final double n = argEvalDoubleValue(context, 1);
    workspace.waitFor
        (new org.nlogo.api.CommandRunnable() {
          public void run() {
            workspace.getHubNetManager().plot(name, n);
          }
        });
    context.ip = next;
  }


}
