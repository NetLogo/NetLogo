// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.I18N
import org.nlogo.nvm.{ Command, Context}
import org.nlogo.nvm.RuntimePrimitiveException

class _tickadvance extends Command {
  switches = true
  override def perform(context: Context) {
    val amount = argEvalDoubleValue(context, 0)
    if(amount < 0)
      throw new RuntimePrimitiveException(
        context, this,  I18N.errors.get("org.nlogo.prim.etc._tickadvance.noNegativeTickAdvances"))
    if(world.tickCounter.ticks == -1)
      throw new RuntimePrimitiveException(
        context, this, I18N.errors.get("org.nlogo.prim.etc.$common.tickCounterNotStarted"))
    world.tickCounter.tick(amount)
    workspace.requestDisplayUpdate(false)
    context.ip = next
  }
}
