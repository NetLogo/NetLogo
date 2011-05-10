package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _fileopen
    extends Command {
  @Override
  public void perform(final org.nlogo.nvm.Context context)
      throws LogoException {
    try {
      // DefaultFileManager.openFile attaches the prefix
      // for us, so we need not normalize our path before
      // calling that method -- CLB 05/17/05
      workspace.fileManager().openFile
          (argEvalString(context, 0));
    } catch (java.io.IOException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
    context.ip = next;
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_STRING};
    return Syntax.commandSyntax(right);
  }
}
