// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Syntax, CommandRunnable }
import org.nlogo.nvm.{ Command, Context, EngineException }

class _moviestart extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.StringType))
  override def perform(context: Context) {
    val path = {
      val arg = argEvalString(context, 0)
      try workspace.fileManager.attachPrefix(arg)
      catch {
        case ex: java.net.MalformedURLException =>
          throw new EngineException(
            context, this, arg + " is not a valid path name: " + ex.getMessage)
      }
    }
    workspace.waitFor(
      new CommandRunnable {
        def run() {
          if (workspace.movieIsOpen)
            throw new EngineException(
              context, _moviestart.this,
              "There is already a movie being made. Must call MOVIE-CLOSE or MOVIE-CANCEL")
          workspace.movieStart(path)
        }})
    context.ip = next
  }
}
