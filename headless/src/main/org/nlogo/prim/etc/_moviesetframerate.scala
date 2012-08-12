// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ CommandRunnable, Syntax }
import org.nlogo.nvm.{ Command, Context, EngineException }

class _moviesetframerate extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.NumberType))
  override def perform(context: Context) {
    val rate = argEvalDoubleValue(context, 0).toFloat
    try workspace.waitFor(
      new CommandRunnable {
        def run() {
          if (!workspace.movieIsOpen)
            throw new EngineException(
              context, _moviesetframerate.this, "Must call MOVIE-START first")
          if (!workspace.movieAnyFramesCaptured)
            throw new EngineException(
              context, _moviesetframerate.this,
              "Can't change frame rate after frames have been grabbed")
          workspace.movieSetRate(rate)
        }})
    catch {
      case ex: java.io.IOException =>
        throw new EngineException(context, this, ex.getMessage)
    }
    context.ip = next
  }
}
