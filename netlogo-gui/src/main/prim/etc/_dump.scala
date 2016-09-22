// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Context, Procedure, Reporter }
import collection.JavaConverters._

class _dump extends Reporter {

  override def report(context: Context) =
    world.program.dump + "\n" +
    workspace.procedures.values
      .toSeq
      .sortWith((p1: Procedure, p2: Procedure) => p1.name < p2.name)
      .map(_.dump)
      .mkString("", "\n", "\n")
}
