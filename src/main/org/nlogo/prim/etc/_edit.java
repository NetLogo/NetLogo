// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Workspace;

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
  public void perform(final Context context) throws LogoException {
    String path = null;
    try {
      path = workspace.convertToNormal();
    } catch (java.io.IOException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
    workspace.outputObject
        ("Now editing: " + path,
            context.agent, true, false,
            Workspace.OutputDestination.NORMAL);
    context.ip = next;
  }
}
