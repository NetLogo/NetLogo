// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _and
    extends Reporter
    implements org.nlogo.core.Pure, org.nlogo.nvm.CustomGenerated {

  @Override
  public int returnType() {
    return Syntax.BooleanType();
  }



  @Override
  public Object report(final Context context) throws LogoException {
    return argEvalBooleanValue(context, 0)
        ? argEvalBoolean(context, 1)
        : Boolean.FALSE;
  }
}
