package org.nlogo.prim.etc;

import org.nlogo.agent.Link;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _isundirectedlink
    extends Reporter {
  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    Object obj = args[0].report(context);
    return obj instanceof Link
        ? (((Link) obj).getBreed().isUndirected() ? Boolean.TRUE : Boolean.FALSE)
        : Boolean.FALSE;
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_WILDCARD};
    int ret = Syntax.TYPE_BOOLEAN;
    return Syntax.reporterSyntax(right, ret);
  }
}




