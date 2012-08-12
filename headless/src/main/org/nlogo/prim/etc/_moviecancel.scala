// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Syntax, CommandRunnable }
import org.nlogo.nvm.{ Command, Context, EngineException }

class _moviecancel extends Command {
  override def syntax =
    Syntax.commandSyntax
  override def perform(context: Context) {
    workspace.waitFor(
      new CommandRunnable {
        def run() {
          workspace.movieCancel()
        }})
    context.ip = next
  }
}
