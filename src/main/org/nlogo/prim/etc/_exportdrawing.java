package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _exportdrawing
    extends org.nlogo.nvm.Command {
  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    final String filePath = argEvalString(context, 0);
    workspace.waitFor
        (new org.nlogo.api.CommandRunnable() {
          public void run() throws LogoException {
            try {
              workspace.exportDrawing
                  (workspace.fileManager().attachPrefix
                      (filePath),
                      "png");
            } catch (java.io.IOException ex) {
              throw new EngineException
                  (context, _exportdrawing.this,
                      token().name() +
                          ": " + ex.getMessage());
            }
          }
        });
    context.ip = next;
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_STRING};
    return Syntax.commandSyntax(right);
  }
}
