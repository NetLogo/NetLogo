// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.OutputDestinationJ;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EditorWorkspace;
import org.nlogo.nvm.RuntimePrimitiveException;

/**
 * makes current model be TYPE_NORMAL. useful for CCL users wanting
 * to commit changes to library models.
 */
public final strictfp class _edit
    extends Command {


  @Override
  public void perform(final Context context) throws LogoException {
    String path = null;
    try {
      path = ((EditorWorkspace) workspace).convertToNormal();
    } catch (java.io.IOException ex) {
      throw new RuntimePrimitiveException(context, this, ex.getMessage());
    }
    workspace.outputObject
        ("Now editing: " + path,
            context.agent, true, false,
            OutputDestinationJ.NORMAL());
    context.ip = next;
  }
}
