// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.api.I18N
import org.nlogo.nvm.{ Command, Context, EngineException }

class _tickadvance extends Command {
  override def syntax =
    SyntaxJ.commandSyntax(Array(Syntax.NumberType), "O---", true)
  override def perform(context: Context) {
    val amount = argEvalDoubleValue(context, 0)
    if(amount < 0)
      throw new EngineException(
        context, this,  I18N.errors.get("org.nlogo.prim.etc._tickadvance.noNegativeTickAdvances"))
    if(world.tickCounter.ticks == -1)
      throw new EngineException(
        context, this, I18N.errors.get("org.nlogo.prim.etc.$common.tickCounterNotStarted"))
    world.tickCounter.tick(amount)
    workspace.requestDisplayUpdate(context, false)
    context.ip = next
  }
}
