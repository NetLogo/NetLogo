// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Reporter;

public final strictfp class _ifelsevalue
    extends Reporter
    implements org.nlogo.nvm.Pure {
  @Override
  public org.nlogo.core.Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.BooleanType(),
            Syntax.ReporterBlockType(),
            Syntax.ReporterBlockType()},
            Syntax.WildcardType());
  }

  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    if (argEvalBooleanValue(context, 0)) {
      return args[1].report(context);
    } else {
      return args[2].report(context);
    }
  }
}
