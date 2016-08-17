// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

public final strictfp class _if
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  @Override
  public void perform(final Context context)
      throws LogoException {
    perform_1(context, argEvalBooleanValue(context, 0));
  }

  @Override
  public String toString() {
    return super.toString() + ":+" + offset;
  }

  public void perform_1(final Context context, boolean arg0) {
    context.ip = arg0 ? next : offset;
  }

  public void assemble(org.nlogo.nvm.AssemblerAssistant a) {
    a.add(this);
    a.block();
    a.resume();
  }
}
