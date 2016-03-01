// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context, EngineException }
import org.nlogo.window.GUIWorkspace

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
    workspace match {
      case gw: GUIWorkspace =>
        workspace.waitFor(
          new org.nlogo.api.CommandRunnable {
            def run() {
              if (gw.movieEncoder != null)
                throw new EngineException(
                  context, _moviestart.this,
                  "There is already a movie being made. Must call MOVIE-CLOSE or MOVIE-CANCEL")
              gw.movieEncoder = new org.nlogo.awt.JMFMovieEncoder(15, path)
            }})
      case _ =>
        throw new EngineException(
          context, this, token.text + " can only be used in the GUI")
    }
    context.ip = next
  }
}
