// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Let
import org.nlogo.api.LogoException
import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Command, Context }

class _let(private[this] val _let: Let) extends Command {
  def let: Let = _let

  @throws(classOf[LogoException])
  override def perform(context: Context): Unit = {
    perform_1(context, args(0).report(context))
  }

  def perform_1(context: Context, value: AnyRef): Unit = {
    context.activation.binding.let(_let, value)
    context.ip = next
  }
}

object _let {
  def unapply(l: _let): Option[Let] =
    Some(l.let)
}
