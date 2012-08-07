// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.EngineException;

public final strictfp class _exportoutput
    extends org.nlogo.nvm.Command {
  @Override
  public void perform(final org.nlogo.nvm.Context context)
      throws LogoException {
    final String filename = argEvalString(context, 0);
    if (filename.equals("")) {
      throw new EngineException
          (context, this, I18N.errorsJ().get("org.nlogo.prim.etc._exportoutput.emptyPath"));
    }
    final org.nlogo.nvm.Command comm = this;
    workspace.waitFor
        (new org.nlogo.api.CommandRunnable() {
          public void run()
              throws EngineException {
            try {
              workspace
                  .exportOutput
                      (workspace.fileManager().attachPrefix
                          (filename));
            } catch (java.io.IOException ex) {
              throw new EngineException(context, comm, ex.getMessage());
            }
          }
        }
        );
    context.ip = next;
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.StringType()};
    return Syntax.commandSyntax(right);
  }
}
