// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Let
import org.nlogo.api.LogoException
import org.nlogo.nvm.{ AssemblerAssistant, Command, Context, CustomAssembled, MutableLong }

class _repeat extends Command with CustomAssembled {
  private[this] val _let: Let = new Let(null)
  def let = _let

  @throws(classOf[LogoException])
  override def perform(context: Context): Unit = {
    perform_1(context, argEvalDoubleValue(context, 0))
  }

  @throws(classOf[LogoException])
  def perform_1(context: Context, d0: Double): Unit = {
    context.let(_let, new MutableLong(validLong(d0)))
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
