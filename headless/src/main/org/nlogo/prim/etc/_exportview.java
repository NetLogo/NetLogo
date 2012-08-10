// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.EngineException;

public final strictfp class _exportview
    extends org.nlogo.nvm.Command {
  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    final String filePath = argEvalString(context, 0);
    workspace.waitFor
        (new org.nlogo.api.CommandRunnable() {
          public void run() throws LogoException {
            try {
              workspace.exportView
                  (workspace.fileManager().attachPrefix
                      (filePath),
                      "png");
            } catch (java.io.IOException ex) {
              throw new EngineException
                  (context, _exportview.this,
                      token().name() +
                          ": " + ex.getMessage());
            }
          }
        });
    context.ip = next;
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.StringType()};
    return Syntax.commandSyntax(right);
  }
}
