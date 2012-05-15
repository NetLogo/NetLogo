// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.CompilerException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _readfromstring
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.StringType()},
            Syntax.ReadableType());
  }

  @Override
  public Object report(final Context context)
      throws LogoException {
    try {
      return workspace.readFromString(argEvalString(context, 0));
    } catch (CompilerException error) {
      throw new EngineException(context, this, error.getMessage());
    }
  }
}
