// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;

public final class _importpcolorsrgb
    extends org.nlogo.nvm.Command {
  public _importpcolorsrgb() {
    this.switches = true;
  }



  @Override
  public void perform(final Context context)
      throws LogoException {
    try {
      org.nlogo.agent.ImportPatchColors.importPatchColors
          (workspace.fileManager().getFile
              (workspace.fileManager().attachPrefix
                  (argEvalString(context, 0))),
              world, false);
    } catch (java.io.IOException ex) {
      throw new RuntimePrimitiveException
          (context, this,
              token().text() +
                  ": " + ex.getMessage());
    }
    context.ip = next;
  }
}
