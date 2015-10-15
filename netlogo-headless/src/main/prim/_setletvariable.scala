// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Let
import org.nlogo.nvm.{ Command, Context }

class _setletvariable(val let: Let) extends Command {
  def this(original: _letvariable) =
    this(original.let)

  override def toString =
    s"${super.toString}:${let.name}"
  override def perform(context: Context) {
    context.setLet(let, args(0).report(context))
    context.ip = next
  }
}
