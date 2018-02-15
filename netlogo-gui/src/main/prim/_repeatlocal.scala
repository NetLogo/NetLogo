// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.LogoException
import org.nlogo.nvm.{AssemblerAssistant, Command, Context, CustomAssembled, MutableLong}
import org.nlogo.prim.etc.{_enterscope, _exitscope}

class _repeatlocal(private[this] val vn: Int) extends Command with CustomAssembled {
  override def toString: String = s"${super.toString}:$vn,+$offset"

  @throws(classOf[LogoException])
  override def perform(context: Context): Unit = {
    perform_1(context, argEvalDoubleValue(context, 0))
  }

  @throws(classOf[LogoException])
  def perform_1(context: Context, arg0: Double): Unit = {
    context.activation.args(vn) = MutableLong(validLong(arg0, context))
    context.ip = offset
  }

  def assemble(a: AssemblerAssistant): Unit = {
    a.add(this)
    a.add(new _enterscope)
    a.block()
    a.add(new _exitscope)
    a.resume()
    a.add(new _repeatlocalinternal(vn, 1 - a.offset))
  }
}
