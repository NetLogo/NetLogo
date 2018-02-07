// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context }

class _thunkdidfinish extends Command {

  override def perform(context: Context) {
    context.activation.binding
      .allLets
      .map(_._1)
      .find(_.name == context.activation.procedure.name + "_finished")
      .foreach { let =>
        context.activation.binding.setLet(let, Boolean.box(true))
      }
    context.ip = next
  }
}
