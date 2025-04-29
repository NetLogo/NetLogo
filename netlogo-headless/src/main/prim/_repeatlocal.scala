// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.nvm.{ AssemblerAssistant, Command, Context, CustomAssembled, MutableLong }

class _repeatlocal(vn: Int) extends Command with CustomAssembled {

  override def toString =
    super.toString + ":" + vn + ",+" + offset

  override def perform(context: Context): Unit = {
    perform_1(context, argEvalDoubleValue(context, 0))
  }

  def perform_1(context: Context, arg0: Double): Unit = {
    context.activation.args(vn) = new MutableLong(validLong(arg0, context))
    context.ip = offset
  }

  override def assemble(a: AssemblerAssistant): Unit = {
    a.add(this)
    a.block()
    a.resume()
    a.add(new _repeatlocalinternal(vn, 1 - a.offset))
  }

}
