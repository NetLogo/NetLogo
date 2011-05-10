package org.nlogo.prim.gui;

import org.nlogo.api.LogoException;
import org.nlogo.api.Nobody;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;
import org.nlogo.window.GUIWorkspace;

public final strictfp class _mousepatch
    extends Reporter {
  @Override
  public Object report(final Context context)
      throws LogoException {
    if (!(workspace instanceof GUIWorkspace)) {
      return Nobody.NOBODY;
    }
    GUIWorkspace gworkspace = (GUIWorkspace) workspace;
    if (!gworkspace.mouseInside()) {
      return org.nlogo.api.Nobody.NOBODY;
    }
    // we must first make sure the event thread has had
    // the opportunity to detect any recent mouse movement
    // - ST 5/3/04, 12/12/06
    workspace.waitForQueuedEvents();
    try {
      return world.getPatchAt(gworkspace.mouseXCor(), gworkspace.mouseYCor());
    } catch (org.nlogo.api.AgentException ex) {
      throw new IllegalStateException(ex);
    }
  }

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.TYPE_PATCH | Syntax.TYPE_NOBODY);
  }
}
