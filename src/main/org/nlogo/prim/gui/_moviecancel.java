package org.nlogo.prim.gui;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;
import org.nlogo.window.GUIWorkspace;

public final strictfp class _moviecancel
    extends org.nlogo.nvm.Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax();
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context)
      throws LogoException {
    if (!(workspace instanceof GUIWorkspace)) {
      throw new EngineException(
          context, this, token().name() + " can only be used in the GUI");
    }
    workspace.waitFor
        (new org.nlogo.api.CommandRunnable() {
          public void run() {
            if (((GUIWorkspace) workspace).movieEncoder != null) {
              ((GUIWorkspace) workspace)
                  .movieEncoder.cancel();
              ((GUIWorkspace) workspace)
                  .movieEncoder = null;
            }
          }
        });
    context.ip = next;
  }
}
