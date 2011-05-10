package org.nlogo.prim.gui;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;
import org.nlogo.window.GUIWorkspace;

public final strictfp class _moviesetframerate
    extends org.nlogo.nvm.Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.TYPE_NUMBER});
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context)
      throws LogoException {
    final float framerate = (float) argEvalDoubleValue(context, 0);
    if (!(workspace instanceof GUIWorkspace)) {
      throw new EngineException(
          context, this, token().name() + " can only be used in the GUI");
    }
    workspace.waitFor
        (new org.nlogo.api.CommandRunnable() {
          public void run() throws LogoException {
            try {
              org.nlogo.awt.MovieEncoder encoder =
                  ((GUIWorkspace) workspace).movieEncoder;

              if (encoder == null) {
                throw new EngineException
                    (context, _moviesetframerate.this,
                        "Must call MOVIE-START first");
              }
              if (encoder.isSetup()) {
                throw new EngineException
                    (context, _moviesetframerate.this,
                        "Can't change frame rate after frames have been grabbed");
              }
              encoder.setFrameRate(framerate);
            } catch (java.io.IOException ex) {
              throw new EngineException
                  (context, _moviesetframerate.this, ex.getMessage());
            }
          }
        });
    context.ip = next;
  }
}
