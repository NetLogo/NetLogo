// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.RuntimePrimitiveException;

public final class _importdrawing
    extends org.nlogo.nvm.Command {
  public _importdrawing() {
    this.switches = true;
  }



  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    if (world.program().dialect().is3D()) {
      throw new RuntimePrimitiveException(context, this,
          I18N.errorsJ().get("org.nlogo.prim.etc._importdrawing.cantImportDrawingin3D"));
    }
    try {
      workspace.importDrawing
          (workspace.fileManager().attachPrefix
              (argEvalString(context, 0)));
    } catch (java.io.IOException ex) {
      throw new RuntimePrimitiveException
          (context, _importdrawing.this,
              token().text() +
                  ": " + ex.getMessage());
    } catch (RuntimeException ex) {
      throw new RuntimePrimitiveException
          (context, _importdrawing.this,
              token().text() +
                  ": " + ex.getMessage());
    }
    context.ip = next;
  }
}
