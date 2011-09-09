package org.nlogo.prim.file;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.api.Syntax;

public final strictfp class _fileexists
    extends Reporter {
  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    try {
      String filePath =
          workspace.fileManager().attachPrefix(argEvalString(context, 0));

      return workspace.fileManager().fileExists(filePath)
          ? Boolean.TRUE
          : Boolean.FALSE;
    } catch (java.net.MalformedURLException ex) {
      throw new EngineException
          (context, _fileexists.this,
              argEvalString(context, 0) + " is not a valid path name: " + ex.getMessage());
    } catch (java.io.IOException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.StringType()};
    int ret = Syntax.BooleanType();
    return Syntax.reporterSyntax(right, ret);
  }
}
