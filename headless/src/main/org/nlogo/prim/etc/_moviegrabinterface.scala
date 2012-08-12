// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Syntax, CommandRunnable }
import org.nlogo.nvm.{ Command, Context, EngineException }

class _moviegrabinterface extends Command {
  override def syntax =
    Syntax.commandSyntax
  override def perform(context: Context) {
    if (world.program.is3D)
      throw new EngineException(
          context, this, token.name + " is not supported in NetLogo 3D")
    if (!workspace.movieIsOpen)
      throw new EngineException(
        context, this, "Must call MOVIE-START first")
    try workspace.waitFor(
      new CommandRunnable {
        def run() {
          workspace.movieGrabInterface()
        }})
    catch {
      case ex: java.io.IOException =>
        throw new EngineException(
          context, this, ex.getMessage)
    }
    context.ip = next
  }
}
