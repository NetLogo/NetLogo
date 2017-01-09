// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.Let;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.MutableLong;

public final strictfp class _waitinternal extends Command {
  private final Let let;

  public _waitinternal(Let let) {
    this.let = let;
    this.switches = true;
  }

  @Override
  public void perform(Context context) {
    perform_1(context);
  }

  public void perform_1(Context context) {
    if (System.nanoTime() >= ((MutableLong) context.activation.binding.getLet(let)).value()) {
      context.ip = next;
    }
  }
}
