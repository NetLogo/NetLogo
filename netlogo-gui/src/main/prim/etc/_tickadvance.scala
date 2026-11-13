// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.LocaleContext
import org.nlogo.nvm.{ Command, Context, RuntimePrimitiveException }

class _tickadvance extends Command {
  switches = true

  override def perform(context: Context): Unit = {
    val amount = argEvalDoubleValue(context, 0)
    if(amount < 0)
      throw new RuntimePrimitiveException(context, this, loc"Cannot advance the tick counter by a negative amount.")
    if(world.tickCounter.ticks == -1)
      throw new RuntimePrimitiveException(context, this,
        loc"The tick counter has not been started yet. Use RESET-TICKS.")
    world.tickCounter.tick(amount)
    workspace.requestDisplayUpdate(false)
    context.ip = next
  }
}
