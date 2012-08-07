// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.EngineException;

public final strictfp class _importworld
    extends org.nlogo.nvm.Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.StringType()},
            "O---", true);
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    final String filePath = argEvalString(context, 0);
    // Workspace.waitFor() switches to the event thread if we're
    // running with a GUI - ST 12/17/04
    workspace.waitFor
        (new org.nlogo.api.CommandRunnable() {
          public void run() throws LogoException {
            try {
              workspace.importWorld
                  (workspace.fileManager().attachPrefix
                      (filePath));
            } catch (java.io.IOException ex) {
              throw new EngineException
                  (context, _importworld.this,
                      token().name() +
                          ": " + ex.getMessage());
            }
          }
        });
    context.ip = next;
  }
}
