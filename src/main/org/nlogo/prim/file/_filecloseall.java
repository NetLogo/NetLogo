package org.nlogo.prim.file;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.EngineException;
import org.nlogo.api.Syntax;

public final strictfp class _filecloseall
    extends Command {
  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    try {
      workspace.fileManager().closeAllFiles();
    } catch (java.io.IOException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
    context.ip = next;
  }

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax();
  }
}
