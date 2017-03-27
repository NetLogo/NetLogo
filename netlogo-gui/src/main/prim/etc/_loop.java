// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.Syntax;
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

  public void assemble(org.nlogo.nvm.AssemblerAssistant a) {
    a.comeFrom();
    a.block();
    a.goTo();
  }
}
