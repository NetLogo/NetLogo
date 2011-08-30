package org.nlogo.prim.gui

import org.nlogo.api.{ Nobody, Syntax }
import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.window.GUIWorkspace

class _mousepatch extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.PatchType | Syntax.NobodyType)
  override def report(context: Context): AnyRef =
    workspace match {
      case gw: GUIWorkspace =>
        if (!gw.mouseInside())
          Nobody
        else {
          // make sure the event thread has had the opportunity to detect any recent mouse movement
          // - ST 5/3/04, 12/12/06
          gw.waitForQueuedEvents();
          world.getPatchAt(gw.mouseXCor, gw.mouseYCor)
        }
      case _ => Nobody
    }
}
