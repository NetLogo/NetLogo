// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Let;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.MutableLong;

public final strictfp class _waitinternal extends Command {
  private final Let let;

  public _waitinternal(Let let) {
    this.let = let;
  }

  @Override
  public org.nlogo.core.Syntax syntax() {
    return Syntax.commandSyntax(true);
  }

  @Override
  public void perform(Context context) {
    perform_1(context);
  }

  public void perform_1(Context context) {
    if (System.nanoTime() >= ((MutableLong) context.getLet(let)).value()) {
      context.ip = next;
    }
  }
}
