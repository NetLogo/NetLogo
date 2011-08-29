package org.nlogo.prim.etc

import org.nlogo.nvm.{ Context, Reporter, Syntax }

class _nopatches extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.TYPE_PATCHSET)
  override def report(context: Context) =
    world.noPatches
}
