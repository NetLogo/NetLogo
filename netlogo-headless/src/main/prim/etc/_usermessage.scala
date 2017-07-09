// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Dump, ReporterRunnable }
import org.nlogo.nvm.{ Command, Context }

class _usermessage extends Command {

  override def perform(context: Context) {
    val message = Dump.logoObject(args(0).report(context))
    workspace.updateUI()
    val canceled = workspace.waitForResult(
      new ReporterRunnable[Boolean] {
        override def run() =
          workspace.userMessage(message)
      })
    if(canceled)
      throw new org.nlogo.nvm.HaltException(true)
    context.ip = next
  }

}
