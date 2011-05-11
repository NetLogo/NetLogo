package org.nlogo.prim.etc

import org.nlogo.nvm.{Context, EngineException, Syntax}
import org.nlogo.api.{I18N, LogoException}

class _tickadvance extends org.nlogo.nvm.Command {
  override def syntax = 
    Syntax.commandSyntax(Array(Syntax.TYPE_NUMBER), "O---", true)
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
