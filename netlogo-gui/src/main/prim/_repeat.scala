// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.{ Let, Token }
import org.nlogo.api.LogoException
import org.nlogo.nvm.{ AssemblerAssistant, Command, Context, CustomAssembled, MutableLong }

class _repeat(_token: Token) extends Command with CustomAssembled {
  token_=(_token)

  private[this] val _let: Let = new Let("~" + _token.text + "_" + _token.start.toString)

  def let = _let

  @throws(classOf[LogoException])
  override def perform(context: Context): Unit = {
    perform_1(context, argEvalDoubleValue(context, 0))
  }

  @throws(classOf[LogoException])
  def perform_1(context: Context, d0: Double): Unit = {
    context.activation.binding.let(_let, new MutableLong(validLong(d0, context)))
    context.ip = offset
  }

  override def toString: String = {
    super.toString() + ":+" + offset
  }

  def assemble(a: AssemblerAssistant): Unit = {
    a.add(this)
    a.block()
    a.resume()
    a.add(new _repeatinternal(1 - a.offset, _let))
  }
}
