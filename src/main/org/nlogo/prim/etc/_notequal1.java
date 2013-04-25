// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Equality;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _notequal1
    extends Reporter
    implements org.nlogo.nvm.Pure {
  final Object value;

  public _notequal1(Object value) {
    this.value = value;
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.WildcardType()};
    int ret = Syntax.BooleanType();
    return Syntax.reporterSyntax(right, ret);
  }

  @Override
  public String toString() {
    return super.toString() + ":" + value;
  }

  @Override
  public Object report(final Context context) {
    return report_1(context, args[0].report(context))
        ? Boolean.TRUE
        : Boolean.FALSE;
  }

  public boolean report_1(final Context context, Object o0) {
    return !Equality.equals(o0, value);
  }
}
