// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.LocaleContextJ;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;

public final class _setpatchsize
    extends Command {

  public _setpatchsize() {
    switches = true;
  }

  @Override
  public void perform(final Context context) {
    final double newPatchSize = argEvalDoubleValue(context, 0);
    if (newPatchSize <= 0) {
      throw new RuntimePrimitiveException(context, this, LocaleContextJ.loc("Patch size must be greater than zero."));
    } else if (newPatchSize != workspace.patchSize()) {
      workspace.waitFor
          (new org.nlogo.api.CommandRunnable() {
            public void run() {
              workspace.setDimensions(
                  workspace.world().getDimensions(), newPatchSize);
            }
          });
      workspace.waitFor
          (new org.nlogo.api.CommandRunnable() {
            public void run() {
              workspace.resizeView();
            }
          });
    }
    context.ip = next;
  }
}
