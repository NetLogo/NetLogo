// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Context, Reporter }
import collection.JavaConverters._

class _dump extends Reporter {

  override def report(context: Context) =
    world.program.dump + "\n" +
    workspace.getProcedures.values.asScala
      .map(_.dump)
      .mkString("", "\n", "\n")
}
