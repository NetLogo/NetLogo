package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _xor
    extends Reporter
    implements org.nlogo.nvm.Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    boolean b1 = argEvalBooleanValue(context, 0);
    boolean b2 = argEvalBooleanValue(context, 1);
    if (b1) {
      return b2 ? Boolean.FALSE : Boolean.TRUE;
    } else {
      return b2 ? Boolean.TRUE : Boolean.FALSE;
    }
  }

  @Override
  public Syntax syntax() {
    int left = Syntax.TYPE_BOOLEAN;
    int[] right = {Syntax.TYPE_BOOLEAN};
    int ret = Syntax.TYPE_BOOLEAN;
    return Syntax.reporterSyntax(left, right, ret,
        Syntax.NORMAL_PRECEDENCE - 6);
  }
}
