// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ CommandRunnable, Syntax }
import org.nlogo.nvm.{ Command, Context, EngineException }

class _exportinterface extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.StringType))
  override def perform(context: Context) {
    val path =
      workspace.fileManager.attachPrefix(
        argEvalString(context, 0))
    workspace.updateUI(context)
    workspace.waitFor(
      new CommandRunnable() {
        override def run() {
          try workspace.exportInterface(path)
          catch {
            case e: java.io.IOException =>
              throw new EngineException(
                context, _exportinterface.this, token.text + ": " + e.getMessage)
          }}})
    context.ip = next
  }
}
