// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm.{ Context, Reporter }

// only display the mainRNG state. the auxiliary shouldn't matter since it doesn't affect the
// outcome of the model.

class _randomstate extends Reporter {
  override def syntax =
    SyntaxJ.reporterSyntax(Syntax.StringType)
  override def report(context: Context): String =
    world.mainRNG.save
}
