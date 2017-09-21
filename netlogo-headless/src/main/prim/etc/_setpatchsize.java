// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.WorldDimensions;
import org.nlogo.api.WorldResizer;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

public final strictfp class _setpatchsize
    extends Command {

  public _setpatchsize() {
    switches = true;
  }

  @Override
  public void perform(final Context context) {
    final double newPatchSize = argEvalDoubleValue(context, 0);
    if (newPatchSize != workspace.patchSize()) {
      final WorldDimensions dim = workspace.world().dimensionsAdjustedForPatchSize(newPatchSize);
      workspace.waitFor
          (new org.nlogo.api.CommandRunnable() {
            public void run() {
              WorldDimensions dim = workspace.world().getDimensions();
              workspace.setDimensions(dim, true, WorldResizer.stopNonObserverJobs());
            }
          });
    }
    context.ip = next;
  }
}
