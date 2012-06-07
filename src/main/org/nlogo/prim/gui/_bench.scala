// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }
import org.nlogo.workspace.{ AbstractWorkspace, Benchmarker }

class _bench extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.NumberType,
                               Syntax.NumberType),
                         "O---")
  override def perform(context: Context) {
    val minTime = argEvalIntValue(context, 0)
    val maxTime = argEvalIntValue(context, 1)
    new Thread("__bench") {
      override def run() {
        Benchmarker.benchmark(
          workspace.asInstanceOf[AbstractWorkspace], minTime, maxTime)
      }
    }.start()
    context.ip = next
  }
}
