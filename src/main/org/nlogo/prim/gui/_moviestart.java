package org.nlogo.prim.gui;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;
import org.nlogo.window.GUIWorkspace;

public final strictfp class _moviestart
    extends org.nlogo.nvm.Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.TYPE_STRING});
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context)
      throws LogoException {
    if (!(workspace instanceof GUIWorkspace)) {
      throw new EngineException(
          context, this, token().name() + " can only be used in the GUI");
    }
    final String filePath;
    try {
      filePath =
          workspace.fileManager().attachPrefix
              (argEvalString(context, 0));
    } catch (java.net.MalformedURLException ex) {
      throw new EngineException
          (context, _moviestart.this,
              argEvalString(context, 0) + " is not a valid path name: " + ex.getMessage());
    }
    workspace.waitFor
        (new org.nlogo.api.CommandRunnable() {
          public void run() throws LogoException {

            org.nlogo.awt.MovieEncoder encoder =
                ((GUIWorkspace) workspace).movieEncoder;

            if (encoder != null) {
              throw new EngineException
                  (context, _moviestart.this,
                      "There is already a movie being made. Must call MOVIE-CLOSE or MOVIE-CANCEL");
            }
            ((GUIWorkspace) workspace).movieEncoder =
                new org.nlogo.awt.JMFMovieEncoder(15, filePath);
          }
        });
    context.ip = next;
  }
}
