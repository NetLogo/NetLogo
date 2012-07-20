// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.MutableLong;

public final strictfp class _repeatlocalinternal
    extends Command {
  private final int vn;

  public _repeatlocalinternal(int vn, int offset) {
    this.vn = vn;
    this.offset = offset;
  }

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax();
  }

  @Override
  public String toString() {
    return super.toString() + ":" + offset + "," + vn;
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) {
    MutableLong counter =
      (MutableLong) context.activation.args()[vn];
    if (counter.value() <= 0) {
      context.ip = next;
    } else {
      counter.value_$eq(counter.value() - 1);
      context.ip = offset;
    }
  }

  public void perform_1(final org.nlogo.nvm.Context context) {
    MutableLong counter =
      (MutableLong) context.activation.args()[vn];
    if (counter.value() <= 0) {
      context.ip = next;
    } else {
      counter.value_$eq(counter.value() - 1);
      context.ip = offset;
    }
  }

}
