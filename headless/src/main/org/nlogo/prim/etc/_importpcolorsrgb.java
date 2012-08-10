// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;

public final strictfp class _importpcolorsrgb
    extends org.nlogo.nvm.Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.StringType()},
            "O---", true);
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
      throw new EngineException
          (context, this,
              token().name() +
                  ": " + ex.getMessage());
    }
    context.ip = next;
  }
}
