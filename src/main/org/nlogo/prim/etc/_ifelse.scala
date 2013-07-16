// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm, nvm.{ Command, Context }

class _ifelse extends Command with nvm.CustomAssembled {

  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.BooleanType,
            Syntax.CommandBlockType,
            Syntax.CommandBlockType))

  override def toString =
    super.toString + ":+" + offset

  override def assemble(a: nvm.AssemblerAssistant) {
    a.add(this)
    a.block(1)
    a.goTo()
    a.resume()
    a.block(2)
    a.comeFrom()
  }

  override def perform(context: Context) {
    perform_1(context, argEvalBooleanValue(context, 0))
  }

  def perform_1(context: Context, arg0: Boolean) {
    context.ip = if (arg0) next else offset
  }

}
