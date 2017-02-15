// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.Dump;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _word
    extends Reporter
    implements Pure, org.nlogo.nvm.CustomGenerated {
  @Override
  public int returnType() {
    return Syntax.StringType();
  }



  @Override
  public Object report(final Context context)
      throws LogoException {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < args.length; i++) {
      result.append(Dump.logoObject(
          args[i].report(context)));
    }
    return result.toString();
  }
}
