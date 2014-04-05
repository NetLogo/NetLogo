// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

public final strictfp class _setpatchsize
    extends Command {
  @Override
  public Syntax syntax() {
    return SyntaxJ.commandSyntax(
        new int[]{Syntax.NumberType()},
        "O---", true);
  }

  @Override
  public void perform(final Context context) {
    final double newPatchSize = argEvalDoubleValue(context, 0);
    if (newPatchSize != workspace.patchSize()) {
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
