// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm
import org.nlogo.nvm.{ Command, Context }

class _if extends Command with nvm.CustomAssembled {

  override def toString =
    super.toString + ":+" + offset

  def assemble(a: nvm.AssemblerAssistant) {
    a.add(this)
    a.block()
    a.resume()
  }

  override def perform(context: Context) {
    perform_1(context, argEvalBooleanValue(context, 0))
  }

  def perform_1(context: Context, arg0: Boolean) {
    context.ip = if (arg0) next else offset
  }

}
