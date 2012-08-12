// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ CommandRunnable, Syntax }
import org.nlogo.nvm.{ Command, Context, EngineException }

class _movieclose extends Command {
  override def syntax =
    Syntax.commandSyntax
  override def perform(context: Context) {
    if (!workspace.movieIsOpen)
      throw new EngineException(
        context, _movieclose.this,
        "Must call MOVIE-START first")
    workspace.waitFor(
      new CommandRunnable {
        override def run() {
          workspace.movieClose()
        }})
    context.ip = next
  }
}
