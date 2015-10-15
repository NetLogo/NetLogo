// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

// This is only used for testing purposes.  It's better not to
// rejigger this, because when we test the bytecode generator, we get
// the disassembly for a reporter r by compiling "__ignore r" and
// we don't want extra stuff in the output. - ST 2/6/09

public final strictfp class _ignore
    extends Command {
  @Override
  public org.nlogo.core.Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.WildcardType()});
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
    args[0].report(context);
    context.ip = next;
  }
}
