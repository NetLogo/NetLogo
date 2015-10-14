// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.{ CommandRunnable, Syntax }
import org.nlogo.nvm.{ Command, Context, EngineException }
import org.nlogo.window.GUIWorkspace

class _exportinterface extends Command {

  override def syntax =
    Syntax.commandSyntax(Array(Syntax.StringType))

  override def perform(context: Context) {
    workspace match {
      case gw: GUIWorkspace =>
        gw.updateUI()
        val filePath = argEvalString(context, 0)
        workspace.waitFor(
          new CommandRunnable() {
            override def run() {
              try workspace.exportInterface(
                workspace.fileManager.attachPrefix(filePath))
              catch {
                case e: java.io.IOException =>
                  throw new EngineException(
                    context, _exportinterface.this, token.name + ": " + e.getMessage)
              }}})
      case _ =>
        throw new EngineException(
          context, this, token.name + " can only be used in the GUI")
    }
    context.ip = next
  }
}
