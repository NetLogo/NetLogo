// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.api.Nobody$;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _patch
    extends Reporter {
  @Override
  public Syntax syntax() {
    int[] right;
    right = new int[]{Syntax.NumberType(),
        Syntax.NumberType()};

    return Syntax.reporterSyntax
        (right, Syntax.PatchType() | Syntax.NobodyType());
  }

  // I've tried to rejigger this and the result gets past TryCatchSafeChecker but then
  // doesn't work at runtime ("Inconsistent stack height") - ST 2/10/09
  @Override
  public Object report(final Context context) throws LogoException {
    try {
      return
          world.getPatchAt
              (argEvalDoubleValue(context, 0),
                  argEvalDoubleValue(context, 1));
    } catch (org.nlogo.api.AgentException ex) {
      return Nobody$.MODULE$;
    }
  }
}
