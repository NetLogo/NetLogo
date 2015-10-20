// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.EngineException;

public final strictfp class _importpatchcolors
    extends org.nlogo.nvm.Command {
  public _importpatchcolors() {
    this.switches = true;
  }

  @Override
  public org.nlogo.core.Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.StringType()},
            "O---");
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context)
      throws LogoException {
    try {
      org.nlogo.agent.ImportPatchColors.importPatchColors
          (workspace.fileManager().getFile
              (workspace.fileManager().attachPrefix
                  (argEvalString(context, 0))),
              world, true);
    } catch (java.io.IOException ex) {
      throw new EngineException
          (context, this,
              token().text() +
                  ": " + ex.getMessage());
    }
    context.ip = next;
  }
}
