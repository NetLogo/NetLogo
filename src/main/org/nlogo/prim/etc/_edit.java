// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.OutputDestinationJ;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;

/**
 * makes current model be TYPE_NORMAL. useful for CCL users wanting
 * to commit changes to library models.
 */
public final strictfp class _edit
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax("O---", false);
  }

  @Override
  public void perform(final Context context) {
    String path = null;
    try {
      path = workspace.convertToNormal();
    } catch (java.io.IOException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
    workspace.outputObject
        ("Now editing: " + path,
            context.agent, true, false,
            OutputDestinationJ.NORMAL());
    context.ip = next;
  }
}
