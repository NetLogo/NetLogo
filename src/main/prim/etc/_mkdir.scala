// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Command, Context }

class _mkdir extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.StringType))
  override def perform(c: Context) {
    // leaving this double-underscored for now since it isn't relative to the model's location, like
    // it ought to be - ST 2/7/11
    new java.io.File(argEvalString(c, 0)).mkdir()
    c.ip = next
  }
}
