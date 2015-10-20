// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.EngineException;

public final strictfp class _importdrawing
    extends org.nlogo.nvm.Command {
  public _importdrawing() {
    this.switches = true;
  }

  @Override
  public org.nlogo.core.Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.StringType()},
            "O---");
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    if (world.program().dialect().is3D()) {
      throw new EngineException(context, this,
          I18N.errorsJ().get("org.nlogo.prim.etc._importdrawing.cantImportDrawingin3D"));
    }
    try {
      workspace.importDrawing
          (workspace.fileManager().attachPrefix
              (argEvalString(context, 0)));
    } catch (java.io.IOException ex) {
      throw new EngineException
          (context, _importdrawing.this,
              token().text() +
                  ": " + ex.getMessage());
    } catch (RuntimeException ex) {
      throw new EngineException
          (context, _importdrawing.this,
              token().text() +
                  ": " + ex.getMessage());
    }
    context.ip = next;
  }
}
