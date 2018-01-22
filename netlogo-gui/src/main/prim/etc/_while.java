// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

public final strictfp class _while
    extends Command
    implements org.nlogo.nvm.CustomAssembled {


  @Override
  public String toString() {
    return super.toString() + ":" + offset;
  }

  @Override
  public void perform(Context context) throws LogoException {
    perform_1(context, argEvalBooleanValue(context, 0));
  }

  public void perform_1(Context context, boolean arg0) {
    context.ip = arg0 ? offset : next;
  }

  public void assemble(org.nlogo.nvm.AssemblerAssistant a) {
    a.goTo();
    a.resume();
    a.add(new _enterscope());
    a.block();
    a.add(new _exitscope());
    a.comeFrom();
    a.add(this);
  }
}
