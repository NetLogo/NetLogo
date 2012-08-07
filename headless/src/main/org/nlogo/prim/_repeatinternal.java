// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.Let;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.MutableLong;

public final strictfp class _repeatinternal
    extends Command {
  private final Let let;

  public _repeatinternal(int offset, Let let) {
    this.offset = offset;
    this.let = let;
  }

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax();
  }

  @Override
  public String toString() {
    return super.toString() + ":" + offset;
  }

  @Override
  public void perform(final Context context) {
    MutableLong counter = (MutableLong) context.getLet(let);
    if (counter.value() <= 0) {
      context.ip = next;
    } else {
      counter.value_$eq(counter.value() - 1);
      context.ip = offset;
    }
  }

  public void perform_1(final Context context) {
    MutableLong counter = (MutableLong) context.getLet(let);
    if (counter.value() <= 0) {
      context.ip = next;
    } else {
      counter.value_$eq(counter.value() - 1);
      context.ip = offset;
    }
  }
}
