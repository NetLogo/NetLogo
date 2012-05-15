// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.Let;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.MutableDouble;

// note that this and _bk are pretty much carbon copies of each other

public final strictfp class _fd
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  final Let let = new Let();

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.NumberType()},
            "-T--");
  }

  @Override
  public void perform(Context context) throws LogoException {
    perform_1(context, argEvalDoubleValue(context, 0));
  }

  public void perform_1(Context context, double d) {
    context.let(let, new MutableDouble(d));
    context.ip = next;
  }

  public void assemble(org.nlogo.nvm.AssemblerAssistant a) {
    a.add(this);
    a.add(new _fdinternal(this));
  }
}
