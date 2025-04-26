// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.nvm.{ Command, Context }

class _orbitright extends Command {


  switches = true
  override def perform(context: Context): Unit = {
    world.observer.orbitRight(argEvalDoubleValue(context, 0))
    context.ip = next
  }
}
