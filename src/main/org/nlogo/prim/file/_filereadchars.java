package org.nlogo.prim.file;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.api.Syntax;

public final strictfp class _filereadchars
    extends Reporter {
  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    int num = argEvalIntValue(context, 0);

    try {
      return workspace.fileManager().readChars(num);
    } catch (java.io.EOFException ex) {
      throw new EngineException(context, this, "The end of file has been reached");
    } catch (java.io.IOException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.NumberType()};
    int ret = Syntax.StringType();
    return Syntax.reporterSyntax(right, ret);
  }
}
