// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.LocaleContextJ;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;

public final class _exportoutput
    extends org.nlogo.nvm.Command {
  @Override
  public void perform(final Context context) {
    final String filename = argEvalString(context, 0);
    if (filename.equals("")) {
      throw new RuntimePrimitiveException(context, this, LocaleContextJ.loc("Can't export to empty pathname."));
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
