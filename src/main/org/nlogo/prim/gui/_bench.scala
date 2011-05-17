package org.nlogo.prim.gui

import org.nlogo.api.LogoException
import org.nlogo.nvm.{ Command, Context, Syntax }
import org.nlogo.workspace.{ AbstractWorkspace, Benchmarker }

class _bench extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.TYPE_NUMBER,
                               Syntax.TYPE_NUMBER),
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
