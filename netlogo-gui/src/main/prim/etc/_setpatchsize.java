// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.JobOwner;
import org.nlogo.api.LogoException;
import org.nlogo.api.WorldResizer;
import org.nlogo.core.Syntax;
import org.nlogo.core.WorldDimensions;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

public final strictfp class _setpatchsize
    extends Command {
  public _setpatchsize() {
    this.switches = true;
  }

  @Override
  public void perform(final Context context) throws LogoException {
    final double newPatchSize = argEvalDoubleValue(context, 0);
    final WorldDimensions dimensions = workspace.world().dimensionsAdjustedForPatchSize(newPatchSize);
    if (newPatchSize != workspace.patchSize()) {
      workspace.waitFor(new org.nlogo.api.CommandRunnable() {
        public void run() {
          workspace.setDimensions(dimensions, true, WorldResizer.stopNonObserverJobs());
        }
      });
    }
    context.ip = next;
  }
}
