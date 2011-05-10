package org.nlogo.prim.gui;

import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

public final strictfp class _beep
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax();
  }

  @Override
  public void perform(final Context context) {
    java.awt.Toolkit.getDefaultToolkit().beep();
    context.ip = next;
  }
}
