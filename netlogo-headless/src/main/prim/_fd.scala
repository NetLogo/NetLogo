// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Let
import org.nlogo.nvm.{ AssemblerAssistant, Command, Context, CustomAssembled, MutableDouble }

// note that this and _bk are pretty much carbon copies of each other

class _fd extends Command with CustomAssembled {

  // MethodRipper won't let us call a public method from perform_1() - ST 7/20/12
  private[this] val _let = Let()
  def let = _let

  override def perform(context: Context) {
    perform_1(context, argEvalDoubleValue(context, 0))
  }

  def perform_1(context: Context, d: Double) {
    context.let(_let, new MutableDouble(d))
    context.ip = next
  }

  def assemble(a: AssemblerAssistant) {
    a.add(this)
    a.add(new _fdinternal(this))
  }

}
