// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Syntax, CommandRunnable }
import org.nlogo.nvm.{ Command, Context, EngineException }

class _moviegrabview extends Command {
  override def syntax =
    Syntax.commandSyntax
  override def perform(context: Context) {
    if (!workspace.movieIsOpen)
      throw new EngineException(
        context, this, "Must call MOVIE-START first")
    try workspace.waitFor(
      new CommandRunnable {
        def run() {
          workspace.movieGrabView()
        }})
    catch {
      case ex: java.io.IOException =>
        throw new EngineException(
          context, this, ex.getMessage)
    }
    context.ip = next
  }
}
