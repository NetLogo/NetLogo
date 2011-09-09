package org.nlogo.prim.file;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.api.Syntax;

public final strictfp class _fileatend
    extends Reporter {
  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    try {
      return workspace.fileManager().eof()
          ? Boolean.TRUE
          : Boolean.FALSE;
    } catch (java.io.IOException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.BooleanType());
  }
}
