// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.core.Let;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.MutableLong;

public final strictfp class _repeat
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  public final Let let = new Let(null);

  @Override
  public org.nlogo.core.Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.NumberType(),
            Syntax.CommandBlockType()});
  }

  @Override
  public void perform(final Context context) throws LogoException {
    perform_1(context, argEvalDoubleValue(context, 0));
  }

  public void perform_1(final Context context, double d0) throws LogoException {
    context.let(let, new MutableLong(validLong(d0)));
    context.ip = offset;
  }

  @Override
  public String toString() {
    return super.toString() + ":+" + offset;
  }

  public void assemble(org.nlogo.nvm.AssemblerAssistant a) {
    a.add(this);
    a.block();
    a.resume();
    a.add(new _repeatinternal(1 - a.offset(), let));
  }
}
