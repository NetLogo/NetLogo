package org.nlogo.prim.etc;

import org.nlogo.agent.Link;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Reporter;
import org.nlogo.api.Syntax;

public final strictfp class _isdirectedlink
    extends Reporter {
  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    Object obj = args[0].report(context);
    return obj instanceof Link
        ? (((Link) obj).getBreed().isDirected() ? Boolean.TRUE : Boolean.FALSE)
        : Boolean.FALSE;
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.WildcardType()};
    int ret = Syntax.BooleanType();
    return Syntax.reporterSyntax(right, ret);
  }
}




