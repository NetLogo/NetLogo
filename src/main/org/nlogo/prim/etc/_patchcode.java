package org.nlogo.prim.etc;

import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.api.Syntax;

public final strictfp class _patchcode
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax("--P-", false);
  }

  @Override
  public void perform(final Context context) {
    throw new UnsupportedOperationException();
  }

  public void assemble(org.nlogo.nvm.AssemblerAssistant a) {
    // do nothing -- drop out of existence
  }
}
