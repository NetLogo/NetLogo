// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

public final strictfp class _loop
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  @Override
  public void perform(final Context context) {
    // we get custom-assembled out of existence
    throw new IllegalStateException();
  }

  @Override
  public org.nlogo.core.Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.CommandBlockType()});
  }

  public void assemble(org.nlogo.nvm.AssemblerAssistant a) {
    a.comeFrom();
    a.block();
    a.goTo();
  }
}
