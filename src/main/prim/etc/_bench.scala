// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm.{ Command, Context }

class _bench extends Command {
  override def syntax =
    SyntaxJ.commandSyntax(Array(Syntax.NumberType,
                               Syntax.NumberType),
                         "O---")
  override def perform(context: Context) {
    val minTime = argEvalIntValue(context, 0)
    val maxTime = argEvalIntValue(context, 1)
    workspace.benchmark(minTime, maxTime)
    context.ip = next
  }
}
