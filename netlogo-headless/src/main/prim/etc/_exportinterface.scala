// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.CommandRunnable
import org.nlogo.nvm.{ Command, Context}
import org.nlogo.nvm.RuntimePrimitiveException

class _exportinterface extends Command {
  override def perform(context: Context) {
    val path =
      workspace.fileManager.attachPrefix(
        argEvalString(context, 0))
    workspace.updateUI()
    workspace.waitFor(
      new CommandRunnable() {
        override def run() {
          try workspace.exportInterface(path)
          catch {
            case e: java.io.IOException =>
              throw new RuntimePrimitiveException(
                context, _exportinterface.this, token.text + ": " + e.getMessage)
          }}})
    context.ip = next
  }
}
