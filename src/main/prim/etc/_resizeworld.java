// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.I18N;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;

public final strictfp class _resizeworld
    extends Command {
  @Override
  public Syntax syntax() {
    return SyntaxJ.commandSyntax(
        new int[]{Syntax.NumberType(), Syntax.NumberType(),
            Syntax.NumberType(), Syntax.NumberType()},
        "O---", true);
  }

  @Override
  public void perform(final Context context) {
    final int newMinX = argEvalIntValue(context, 0);
    final int newMaxX = argEvalIntValue(context, 1);
    final int newMinY = argEvalIntValue(context, 2);
    final int newMaxY = argEvalIntValue(context, 3);

    final int oldMinX = workspace.world().minPxcor();
    final int oldMaxX = workspace.world().maxPxcor();
    final int oldMinY = workspace.world().minPycor();
    final int oldMaxY = workspace.world().maxPycor();

    if (newMinX > 0 || newMaxX < 0 || newMinY > 0 || newMaxY < 0) {
      throw new EngineException
          (context, this, I18N.errorsJ().get("org.nlogo.prim.etc._resizeworld.worldMustIncludeOrigin"));
    }

    if (oldMinX != newMinX || oldMaxX != newMaxX ||
        oldMinY != newMinY || oldMaxY != newMaxY) {
      workspace.waitFor
          (new org.nlogo.api.CommandRunnable() {
            public void run() {
              workspace.setDimensions
                  (new org.nlogo.core.WorldDimensions(newMinX, newMaxX,
                      newMinY, newMaxY));
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
