// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.{ CommandRunnable, Syntax }
import org.nlogo.awt.MovieEncoder
import org.nlogo.nvm.{ Command, Context, EngineException }
import org.nlogo.window.GUIWorkspace

class _moviesetframerate extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.NumberType))
  override def perform(context: Context) {
    val rate = argEvalDoubleValue(context, 0).toFloat
    workspace match {
      case gw: GUIWorkspace =>
        workspace.waitFor(
          new CommandRunnable {
            def run() {
              setRate(context, gw.movieEncoder, rate)
            }})
      case _ =>
        throw new EngineException(
          context, this, token.name + " can only be used in the GUI")
    }
    context.ip = next
  }
  private def setRate(context: Context, encoder: MovieEncoder, rate: Float) {
    if (encoder == null)
      throw new EngineException(
        context, this, "Must call MOVIE-START first")
    if (encoder.isSetup)
      throw new EngineException(
        context, this,
        "Can't change frame rate after frames have been grabbed")
    try encoder.setFrameRate(rate)
    catch {
      case ex: java.io.IOException =>
        throw new EngineException(context, this, ex.getMessage)
    }
  }
}
