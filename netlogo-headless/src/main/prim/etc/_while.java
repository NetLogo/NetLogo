// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.CompilerScoping;
import org.nlogo.nvm.CustomAssembled;

public final class _while
    extends Command
    implements CustomAssembled, CompilerScoping {

  @Override
  public String toString() {
    return super.toString() + ":" + offset;
  }

  @Override
  public void perform(Context context) {
    perform_1(context, argEvalBooleanValue(context, 0));
  }

  public void perform_1(Context context, boolean arg0) {
    context.ip = arg0 ? offset : next;
  }

  public void assemble(org.nlogo.nvm.AssemblerAssistant a) {
    a.goTo();
    a.resume();
    a.block();
    a.comeFrom();
    a.add(this);
  }

  public int scopedBlockIndex() {
    return 1;
  }
}
