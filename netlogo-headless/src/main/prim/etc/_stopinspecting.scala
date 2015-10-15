// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Context, Command }

class _stopinspecting extends Command {
  override def perform(context: Context) {
    context.ip = next
  }
}

class _stopinspectingdeadagents extends Command {
  override def perform(context: Context) {
    context.ip = next
  }
}
