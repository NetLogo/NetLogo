// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.hubnet;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;

public final strictfp class _hubnetmakeplotnarrowcast
    extends HubNetCommand {


  @Override
  public void perform(final Context context)
      throws LogoException {
    final String name = argEvalString(context, 0);

    workspace.waitFor
        (new org.nlogo.api.CommandRunnable() {
          public void run() throws LogoException {
            if (!hubNetManager().get().addNarrowcastPlot(name)) {
              throw new RuntimePrimitiveException
                  (context, _hubnetmakeplotnarrowcast.this,
                      "no such plot: \"" + name + "\"");
            }
          }
        });
    context.ip = next;
  }
}
