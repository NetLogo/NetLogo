// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.LocaleContextJ;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.RuntimePrimitiveException;

public final class _exportoutput
    extends org.nlogo.nvm.Command {
  @Override
  public void perform(final org.nlogo.nvm.Context context)
      throws LogoException {
    final String filename = argEvalString(context, 0);
    if (filename.equals("")) {
      throw new RuntimePrimitiveException(context, this, LocaleContextJ.loc("Can't export to empty pathname."));
    }
    final org.nlogo.nvm.Command comm = this;
    try {
      workspace.exportOutput(workspace.fileManager().attachPrefix(filename));
    } catch (java.io.IOException ex) {
      throw new RuntimePrimitiveException(context, comm, ex.getMessage());
    }
    context.ip = next;
  }


}
