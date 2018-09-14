// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.nvm.{ Command, Context }

class _goto extends Command {

  override def toString: String = super.toString + ":" + offset

  override def perform(context: Context): Unit = perform_1(context)

  def perform_1(context: Context): Unit = context.ip = offset
}
