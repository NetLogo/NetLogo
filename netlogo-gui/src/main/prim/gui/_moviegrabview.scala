// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context, EngineException }
import org.nlogo.window.GUIWorkspace

class _moviegrabview extends Command {
  override def syntax =
    Syntax.commandSyntax
  override def perform(context: Context) {
    workspace match {
      case gw: GUIWorkspace =>
        workspace.waitFor(
          new org.nlogo.api.CommandRunnable {
            def run() {
              try {
                if (gw.movieEncoder == null)
                  throw new EngineException(
                    context, _moviegrabview.this, "Must call MOVIE-START first")
                gw.movieEncoder.add(gw.exportView())
              }
              catch {
                case ex: java.io.IOException =>
                  throw new EngineException(
                    context, _moviegrabview.this, ex.getMessage)
              }}})
      case _ =>
        throw new EngineException(
          context, this, token.text + " can only be used in the GUI")
    }
    context.ip = next
  }
}
