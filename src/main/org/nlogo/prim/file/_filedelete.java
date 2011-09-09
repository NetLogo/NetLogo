package org.nlogo.prim.file;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.EngineException;
import org.nlogo.api.Syntax;

public final strictfp class _filedelete
    extends Command {
  @Override
  public void perform(final org.nlogo.nvm.Context context)
      throws LogoException {
    try {
      String filePath =
          workspace.fileManager().attachPrefix
              (argEvalString(context, 0));

      workspace.fileManager().deleteFile(filePath);
    } catch (java.net.MalformedURLException ex) {
      throw new EngineException
          (context, _filedelete.this,
              argEvalString(context, 0) + " is not a valid path name: " + ex.getMessage());
    } catch (java.io.IOException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
    context.ip = next;
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.StringType()};
    return Syntax.commandSyntax(right);
  }
}
