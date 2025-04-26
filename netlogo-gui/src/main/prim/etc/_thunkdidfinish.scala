// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context }

class _thunkdidfinish extends Command {

  override def perform(context: Context): Unit = {
    workspace.completedActivations.put(context.activation, true)
    context.ip = next
  }
}
