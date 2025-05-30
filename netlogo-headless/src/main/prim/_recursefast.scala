// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.nvm.{ Command, Context }

class _recursefast(original: _call) extends Command {

  token = original.token

  override def toString =
    super.toString + ":" + offset

  override def perform(context: Context): Unit = {
    perform_1(context)
  }

  def perform_1(context: Context): Unit = {
    if (context.atTopActivation)
      context.ip = offset
    else
      // if we're inside an ask inside the current procedure, then we have
      // to do normal recursion, not "fast" tail recursion - ST 11/17/04
      original.perform(context)
  }

}
