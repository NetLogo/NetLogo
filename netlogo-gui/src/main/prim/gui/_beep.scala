// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.nvm.{ Command, Context }

class _beep extends Command {

  override def perform(context: Context): Unit = {
    java.awt.Toolkit.getDefaultToolkit().beep()
    context.ip = next
  }
}
