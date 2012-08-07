// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.Dump;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _word
    extends Reporter
    implements Pure, org.nlogo.nvm.CustomGenerated {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.RepeatableType() | Syntax.WildcardType()},
            Syntax.StringType(), 2, 0);
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
