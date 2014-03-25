// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }

class _foreverbuttonend extends Command {

  override def syntax =
    Syntax.commandSyntax(true)

  override def perform(context: Context) {
    context.job.buttonTurnIsOver = true
    // remember, the stopping flag on jobs is for the user stopping a forever button by clicking it;
    // the stopping flag on contexts is for the forever button stopped because the procedure invoked
    // by the button used the "stop" command to exit
    if (context.job.stopping || context.stopping)
      context.finished = true
    else
      context.ip = next
  }

}
