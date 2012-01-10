// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;

public final strictfp class _exportworld
    extends org.nlogo.nvm.Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.StringType()});
  }

  @Override
  public void perform(final Context context) throws LogoException {
    final String filePath = argEvalString(context, 0);
    // Workspace.waitFor() switches to the event thread if we're
    // running with a GUI - ST 12/17/04
    workspace.waitFor
        (new org.nlogo.api.CommandRunnable() {
          public void run() throws LogoException {
            try {
              workspace.exportWorld
                  (workspace.fileManager()
                      .attachPrefix(filePath));
            } catch (java.net.MalformedURLException ex) {
              throw new EngineException
                  (context, _exportworld.this,
                      token().name() +
                          ": " + ex.getMessage());
            } catch (java.io.IOException ex) {
              throw new EngineException
                  (context, _exportworld.this,
                      token().name() +
                          ": " + ex.getMessage());
            }
          }
        });
    context.ip = next;
  }
}
