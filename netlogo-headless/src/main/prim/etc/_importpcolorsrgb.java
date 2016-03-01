// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;

public final strictfp class _importpcolorsrgb
    extends org.nlogo.nvm.Command {

  public _importpcolorsrgb() {
    switches = true;
  }

  @Override
  public void perform(final Context context) {
    try {
      org.nlogo.agent.ImportPatchColors.importPatchColors
          (workspace.fileManager().getFile
              (workspace.fileManager().attachPrefix
                  (argEvalString(context, 0))),
              world, false);
    } catch (java.io.IOException ex) {
      throw new EngineException
          (context, this,
              token().text() +
                  ": " + ex.getMessage());
    }
    context.ip = next;
  }
}
