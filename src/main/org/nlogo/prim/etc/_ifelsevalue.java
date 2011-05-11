package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _ifelsevalue
    extends Reporter
    implements org.nlogo.nvm.Pure {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.TYPE_BOOLEAN,
            Syntax.TYPE_REPORTER_BLOCK,
            Syntax.TYPE_REPORTER_BLOCK},
            Syntax.TYPE_WILDCARD);
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
