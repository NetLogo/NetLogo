package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.EngineException;
import org.nlogo.api.Syntax;

public final strictfp class _fileclose
    extends Command {
  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    try {
      if (workspace.fileManager().hasCurrentFile()) {
        workspace.fileManager().closeCurrentFile();
      }
    } catch (java.io.IOException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
    context.ip = next;
  }

  @Override
  public Syntax syntax() {
    int[] right = {};
    return Syntax.commandSyntax(right);
  }
}
