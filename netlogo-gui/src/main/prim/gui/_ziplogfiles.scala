// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.log.LogManager
import org.nlogo.nvm.{ Command, Context }

class _ziplogfiles extends Command {
  switches = true

  override def perform(context: Context): Unit = {
    val zipFilePath = argEvalString(context, 0)
    LogManager.zipLogFiles(zipFilePath)
    context.ip = next
  }
}
