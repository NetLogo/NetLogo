// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.{ Let, Token }
import org.nlogo.nvm.{ AssemblerAssistant, Command, Context, CustomAssembled, MutableDouble }

// note that this and _fd are pretty much carbon copies of each other

class _bk(_token: Token) extends Command with CustomAssembled {
  token_=(_token)

  // MethodRipper won't let us call a public method from perform_1() - ST 7/20/12
  private[this] val _let = Let(s"~${_token.text}_${_token.start}")

  def let = _let

  override def perform(context: Context): Unit = {
    perform_1(context, argEvalDoubleValue(context, 0))
  }

  def perform_1(context: Context, d: Double): Unit = {
    context.activation.binding.let(_let, new MutableDouble(-d))
    context.ip = next
  }

  def assemble(a: AssemblerAssistant): Unit = {
    a.add(this)
    a.add(new _fdinternal(this))
  }
}
