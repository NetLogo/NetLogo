// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ I18N, Syntax }
import org.nlogo.nvm.{ Command, Context, EngineException }

class _tickadvance extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.NumberType), "O---", true)
  override def perform(context: Context) {
    val amount = argEvalDoubleValue(context, 0)
    if(amount < 0)
      throw new EngineException(
        context, this,  I18N.errors.get("org.nlogo.prim.etc._tickadvance.noNegativeTickAdvances"))
    if(world.tickCounter.ticks == -1)
      throw new EngineException(
        context, this, I18N.errors.get("org.nlogo.prim.etc.$common.tickCounterNotStarted"))
    world.tickCounter.tick(amount)
    workspace.requestDisplayUpdate(false)
    context.ip = next
  }
}
