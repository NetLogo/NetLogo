// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.core.I18N;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;

public final class _setpatchsize
    extends Command {
  public _setpatchsize() {
    this.switches = true;
  }



  @Override
  public void perform(final Context context) throws LogoException {
    final double newPatchSize = argEvalDoubleValue(context, 0);
    if (newPatchSize <= 0) {
      throw new RuntimePrimitiveException(context, this, I18N.errorsJ().get("org.nlogo.prim.etc._setpatchsize.positive"));
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
