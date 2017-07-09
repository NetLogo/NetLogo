// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.nvm.{ Context, Reporter }

class _oycor extends Reporter {

  override def report(context: Context) =
    Double.box(world.observer.oycor)
}
