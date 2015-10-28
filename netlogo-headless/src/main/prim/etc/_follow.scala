// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Turtle
import org.nlogo.core.{ I18N, Syntax }
import org.nlogo.api.{ LogoException, Perspective }
import org.nlogo.nvm.{ Command, Context, EngineException }

class _follow extends Command {
  this.switches = true;

  override def perform(context: Context): Unit = {
    val turtle = argEvalTurtle(context, 0)
    if (turtle.id == -1) {
      throw new EngineException(context, this,
        I18N.errors.getN("org.nlogo.$common.thatAgentIsDead", turtle.classDisplayName()))
    }
    val distance = (turtle.size * 5).toInt
    val followDistance = 1 max distance min 100
    world.observer.setPerspective(Perspective.Follow(turtle, followDistance))
    context.ip = next;
  }
}
