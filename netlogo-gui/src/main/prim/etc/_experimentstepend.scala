// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context }

class _experimentstepend extends Command {

  override def perform(context: Context): Unit = {
    if (context.stopping)
      // Worker checks this flag to see if the run should be stopped - ST 3/8/06
      context.job.stopping = true
    context.finished = true
  }
}
