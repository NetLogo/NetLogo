package org.nlogo.prim.gui;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Syntax;
import org.nlogo.window.GUIWorkspace;

public final strictfp class _mousedown
    extends org.nlogo.nvm.Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.TYPE_BOOLEAN);
  }

  @Override
  public Object report(final org.nlogo.nvm.Context context)
      throws LogoException {
    boolean b = false;
    if (workspace instanceof GUIWorkspace) {
      b = ((GUIWorkspace) workspace).mouseDown();
    }
    return b ? Boolean.TRUE : Boolean.FALSE;
  }
}
