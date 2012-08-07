// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Dump, Syntax }
import org.nlogo.nvm.{ Command, Context, EngineException }

class _error extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.WildcardType))
  override def perform(context: Context) {
    throw new EngineException(context, this,
      Dump.logoObject(args(0).report(context)))
  }
}
