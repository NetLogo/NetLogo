// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.core.Let;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

public final strictfp class _setletvariable
    extends Command {
  public final Let let;
  private final String name;

  public _setletvariable(_letvariable original) {
    let = original.let;
    name = original.name;
  }

  @Override
  public void perform(final Context context) throws LogoException {
    context.setLet(let, args[0].report(context));
    context.ip = next;
  }

  @Override
  public org.nlogo.core.Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.WildcardType()});
  }

  @Override
  public String toString() {
    return super.toString() + ":" + name;
  }
}
