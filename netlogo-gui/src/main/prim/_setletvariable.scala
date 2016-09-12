// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Let
import org.nlogo.api.LogoException
import org.nlogo.nvm.{ Command, Context }

class _setletvariable(original: _letvariable) extends Command {
  private[this] val _let = original.let
  private[this] val name = original.name
  def let = _let

  @throws(classOf[LogoException])
  override def perform(context: Context): Unit = {
    perform_1(context, args(0).report(context))
  }

  def perform_1(context: Context, value: AnyRef): Unit = {
    context.setLet(_let, value)
    context.ip = next
  }

  override def toString: String =
    super.toString + ":" + name
}
