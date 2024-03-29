// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.nvm.RuntimePrimitiveException;

public final class _importworld
    extends org.nlogo.nvm.Command {

  public _importworld() {
    switches = true;
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) {
    final String filePath = argEvalString(context, 0);
    // Workspace.waitFor() switches to the event thread if we're
    // running with a GUI - ST 12/17/04
    workspace.waitFor
        (new org.nlogo.api.CommandRunnable() {
          public void run() {
            try {
              workspace.importWorld
                  (workspace.fileManager().attachPrefix
                      (filePath));
            } catch (java.io.IOException ex) {
              throw new RuntimePrimitiveException
                  (context, _importworld.this,
                      token().text() +
                          ": " + ex.getMessage());
            }
          }
        });
    context.ip = next;
  }
}
