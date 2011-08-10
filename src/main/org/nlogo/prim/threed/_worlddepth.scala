package org.nlogo.prim.threed

import org.nlogo.agent.World3D
import org.nlogo.nvm.{ Context, Reporter, Syntax }

class _worlddepth extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.TYPE_NUMBER)
  override def report(context: Context) =
    java.lang.Double.valueOf(world.asInstanceOf[World3D].worldDepth)
}
