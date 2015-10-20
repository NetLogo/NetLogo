// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;

public final strictfp class _setprocedurevariable
    extends Command {
  private final int vn;
  private final String name;

  public _setprocedurevariable(_procedurevariable original) {
    vn = original.vn;
    name = original.name;
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

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    perform_1(context, args[0].report(context));
  }

  public void perform_1(final org.nlogo.nvm.Context context, Object arg0) {
    context.activation.args[vn] = arg0;
    context.ip = next;
  }
}
