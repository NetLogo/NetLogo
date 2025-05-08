// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Let
import org.nlogo.nvm.{ Command, Context }

class _let(_let: Let) extends Command {

  def let: Let = _let

  override def perform(context: Context): Unit = {
    perform_1(context, args(0).report(context))
  }

  def perform_1(context: Context, arg0: AnyRef): Unit = {
    context.activation.binding.let(_let, arg0)
    context.ip = next
  }
}
