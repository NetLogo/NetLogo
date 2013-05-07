// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

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
  public void perform(final Context context) {
    final String filePath = argEvalString(context, 0);
    // Workspace.waitFor() switches to the event thread if we're
    // running with a GUI - ST 12/17/04
    workspace.waitFor
        (new org.nlogo.api.CommandRunnable() {
          public void run() {
            try {
              workspace.exportWorld
                  (workspace.fileManager()
                      .attachPrefix(filePath));
            } catch (java.net.MalformedURLException ex) {
              throw new EngineException
                  (context, _exportworld.this,
                      token().text() +
                          ": " + ex.getMessage());
            } catch (java.io.IOException ex) {
              throw new EngineException
                  (context, _exportworld.this,
                      token().text() +
                          ": " + ex.getMessage());
            } catch (java.lang.IllegalStateException ex) {
                // This exception is thrown when `DefaultFileManager.relativeToAbsolute` takes an `IOException` and wraps it up on failure.
                // It's hard to judge what ramifications it would have to avoid wrapping that exception, so we'll just catch it here.
                // --JAB (1/9/13)
                throw new EngineException
                    (context, _exportworld.this,
                        token().text() +
                            ": " + ex.getMessage());
            }
          }
        });
    context.ip = next;
  }
}
