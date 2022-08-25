// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.I18N;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;

public final class _exportoutput
    extends org.nlogo.nvm.Command {
  @Override
  public void perform(final Context context) {
    final String filename = argEvalString(context, 0);
    if (filename.equals("")) {
      throw new RuntimePrimitiveException
          (context, this, I18N.errorsJ().get("org.nlogo.prim.etc._exportoutput.emptyPath"));
    }
    final org.nlogo.nvm.Command comm = this;
    workspace.waitFor
        (new org.nlogo.api.CommandRunnable() {
          public void run()
              throws RuntimePrimitiveException {
            try {
              workspace
                  .exportOutput
                      (workspace.fileManager().attachPrefix
                          (filename));
            } catch (java.io.IOException ex) {
              throw new RuntimePrimitiveException(context, comm, ex.getMessage());
            }
          }
        }
        );
    context.ip = next;
  }

}
