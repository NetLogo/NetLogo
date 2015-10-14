// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

// In testing I sometimes use this as an example of an unrejiggered
// primitive, so don't rejigger it (unless you go find and change
// those test cases). - ST 2/6/09

public final strictfp class _boom
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.WildcardType());
  }

  @Override
  public Object report(Context context)
      throws LogoException {
    throw new EngineException(context, this, "boom!");
  }
}
