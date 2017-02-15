// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Let
import org.nlogo.api.LogoException
import org.nlogo.nvm.{ Command, Context }

class _setletvariable(private[this] val _let: Let) extends Command {
  def this(original: _letvariable) = this(original.let)

  def let = _let

  @throws(classOf[LogoException])
  override def perform(context: Context): Unit = {
    perform_1(context, args(0).report(context))
  }

  def perform_1(context: Context, value: AnyRef): Unit = {
    context.activation.binding.setLet(_let, value)
    context.ip = next
  }

  override def toString: String =
    s"${super.toString}:${let.name}"
}
