// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }
import org.nlogo.workspace.{ AbstractWorkspaceScala, Benchmarker }

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
          workspace.asInstanceOf[AbstractWorkspaceScala], minTime, maxTime)
      }
    }.start()
    context.ip = next
  }
}
