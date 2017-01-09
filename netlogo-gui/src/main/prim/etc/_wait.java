// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.Let;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.MutableLong;

public final strictfp class _wait
    extends Command
    implements org.nlogo.nvm.CustomAssembled {

  private final Let let = new Let("~WAITCOUNTER");

  @Override
  public void perform(final Context context)
      throws LogoException {
    long targetTime = System.nanoTime();
    targetTime += argEvalDoubleValue(context, 0) * 1000000000;
    context.activation.binding.let(let, new MutableLong(targetTime));
    context.ip = next;
  }

  public void assemble(org.nlogo.nvm.AssemblerAssistant a) {
    a.add(this);
    a.add(new _waitinternal(let));
  }
}
