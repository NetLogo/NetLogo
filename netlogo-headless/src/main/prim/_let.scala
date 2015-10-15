// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Let
import org.nlogo.nvm.{ Command, Context }

class _let(private[this] val _let: Let) extends Command {

  def let: Let = _let

  override def perform(context: Context) {
    perform_1(context, args(0).report(context))
  }

  def perform_1(context: Context, arg0: AnyRef) {
    context.let(_let, arg0)
    context.ip = next
  }
}
