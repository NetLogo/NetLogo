// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.I18N;
import org.nlogo.core.WorldDimensions;
import org.nlogo.api.LogoException;
import org.nlogo.api.WorldResizer;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;

public final strictfp class _resizeworld
    extends Command {
  public _resizeworld() {
    this.switches = true;
  }

  @Override
  public void perform(final Context context) throws LogoException {
    final int newMinX = argEvalIntValue(context, 0);
    final int newMaxX = argEvalIntValue(context, 1);
    final int newMinY = argEvalIntValue(context, 2);
    final int newMaxY = argEvalIntValue(context, 3);

    final int oldMinX = workspace.world().minPxcor();
    final int oldMaxX = workspace.world().maxPxcor();
    final int oldMinY = workspace.world().minPycor();
    final int oldMaxY = workspace.world().maxPycor();

    if (newMinX > 0 || newMaxX < 0 || newMinY > 0 || newMaxY < 0) {
      throw new RuntimePrimitiveException
          (context, this, I18N.errorsJ().get("org.nlogo.prim.etc._resizeworld.worldMustIncludeOrigin"));
    }

    final double patchSize = workspace.world().patchSize();
    final boolean wrappingAllowedInX = workspace.world().wrappingAllowedInX();
    final boolean wrappingAllowedInY = workspace.world().wrappingAllowedInY();

    if (oldMinX != newMinX || oldMaxX != newMaxX ||
        oldMinY != newMinY || oldMaxY != newMaxY) {
      final WorldDimensions dimensions =
        new org.nlogo.core.WorldDimensions(newMinX, newMaxX,
            newMinY, newMaxY, patchSize, wrappingAllowedInX, wrappingAllowedInY);
      workspace.waitFor(
          new org.nlogo.api.CommandRunnable() {
            public void run() {
              workspace.setDimensions(dimensions, true, WorldResizer.stopNonObserverJobs());
            }
          });
    }
    context.ip = next;
  }
}
