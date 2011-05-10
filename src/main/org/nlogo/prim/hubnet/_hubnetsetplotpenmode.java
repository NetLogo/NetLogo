package org.nlogo.prim.hubnet;

import org.nlogo.api.LogoException;
import org.nlogo.api.PlotPenInterface;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _hubnetsetplotpenmode
    extends org.nlogo.nvm.Command {
  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    final String name = argEvalString(context, 0);
    final int mode = argEvalIntValue(context, 1);
    workspace.waitFor
        (new org.nlogo.api.CommandRunnable() {
          public void run() throws LogoException {
            if (mode < PlotPenInterface.MIN_MODE || mode > PlotPenInterface.MAX_MODE) {
              throw new EngineException
                  (context, _hubnetsetplotpenmode.this, mode
                      + " is not a valid plot pen mode "
                      + "(valid modes are 0, 1, and 2)");
            }
            workspace.getHubNetManager().setPlotPenMode(name, mode);
          }
        });
    context.ip = next;
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_STRING, Syntax.TYPE_NUMBER};
    return Syntax.commandSyntax(right);
  }
}
