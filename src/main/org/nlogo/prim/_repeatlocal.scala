// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context, MutableLong,
                       CustomAssembled, AssemblerAssistant }

class _repeatlocal(vn: Int) extends Command with CustomAssembled {

  override def syntax =
    Syntax.commandSyntax(Array(Syntax.NumberType,
                               Syntax.CommandBlockType))

  override def toString =
    super.toString + ":" + vn + ",+" + offset

  override def perform(context: Context) {
    perform_1(context, argEvalDoubleValue(context, 0))
  }

  def perform_1(context: Context, arg0: Double) {
    context.activation.args(vn) = new MutableLong(validLong(arg0))
    context.ip = offset
  }

  override def assemble(a: AssemblerAssistant) {
    a.add(this)
    a.block()
    a.resume()
    a.add(new _repeatlocalinternal(vn, 1 - a.offset))
  }

}
