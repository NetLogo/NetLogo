// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

public final strictfp class _goto  // , Matt
    extends Command {
  public _goto(int offset) {
    this.offset = offset;
  }



  @Override
  public String toString() {
    return super.toString() + ":" + offset;
  }

  @Override
  public void perform(final Context context) {
    context.ip = offset;
  }

  public void perform_1(final Context context) {
    context.ip = offset;
  }
}
