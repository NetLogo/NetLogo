package org.nlogo.prim.file;

import org.nlogo.api.CompilerException;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.api.Syntax;

public final strictfp class _fileread
    extends Reporter {
  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    Object obj = null;

    try {
      obj = workspace.fileManager().read(world);
    } catch (CompilerException error) {
      try {
        String newErrorDescription =
            error.getMessage() + workspace.fileManager().getErrorInfo();
        throw new EngineException(context, this, newErrorDescription);
      } catch (java.io.IOException ex) {
        throw new IllegalStateException(ex);
      }
    } catch (java.io.EOFException ex) {
      throw new EngineException(context, this, "The end of file has been reached");
    } catch (java.io.IOException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }

    return obj;
  }

  @Override
  public Syntax syntax() {
    int[] right = {};
    int ret = Syntax.ReadableType();
    return Syntax.reporterSyntax(right, ret);
  }
}
