// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.{ CommandRunnable, Syntax }
import org.nlogo.nvm.{ Command, Context, EngineException }
import org.nlogo.window.GUIWorkspace

class _movieclose extends Command {
  override def syntax =
    Syntax.commandSyntax
  override def perform(context: Context) {
    workspace match {
      case gw: GUIWorkspace =>
        workspace.waitFor(
          new CommandRunnable {
            override def run() {
              if (gw.movieEncoder == null)
                throw new EngineException(context, _movieclose.this,
                      "Must call MOVIE-START first");
              org.nlogo.swing.ModalProgressTask.apply(
                gw.getFrame, "Exporting movie...",
                new Runnable() {
                  override def run() {
                    gw.movieEncoder.stop()
                    gw.movieEncoder = null
                  }})}})
      case _ =>
        throw new EngineException(
          context, this, token.name + " can only be used in the GUI")
    }
    context.ip = next
  }
}
