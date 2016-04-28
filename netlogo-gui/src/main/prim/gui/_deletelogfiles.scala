// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Command, Context }

class _deletelogfiles extends Command {


  switches = true
  override def perform(context: Context) {
    workspace.deleteLogFiles()
    context.ip = next
  }
}
