package org.nlogo.prim.etc

import org.nlogo.api.LogoException
import org.nlogo.nvm.{Context, EngineException, Syntax}

class _tickadvance extends org.nlogo.nvm.Command {
  override def syntax = 
    Syntax.commandSyntax(Array(Syntax.TYPE_NUMBER), "O---", true)
  override def perform(context: Context) {
    val amount = argEvalDoubleValue(context, 0)
    if(amount < 0)
      throw new EngineException(
        context, this,
        "Cannot advance the tick counter by a negative amount")
    if(world.tickCounter.ticks == -1)
      throw new EngineException(
        context, this,
        "The tick counter has not been started yet. Use RESET-TICKS." )
    world.tickCounter.tick(amount)
    workspace.requestDisplayUpdate(false)
    context.ip = next
  }
}
