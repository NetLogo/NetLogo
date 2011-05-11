package org.nlogo.prim.etc;

import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

public final strictfp class _observercode
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  @Override
  public void perform(final Context context) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax("O---", false);
  }

  public void assemble(org.nlogo.nvm.AssemblerAssistant a) {
    // do nothing -- drop out of existence
  }
}
