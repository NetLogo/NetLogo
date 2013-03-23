// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, Let }
import org.nlogo.nvm.{ Command, Context }

class _setletvariable(val let: Let) extends Command {
  def this(original: _letvariable) =
    this(original.let)
  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.WildcardType))
  override def toString =
    super.toString + ":" + let.name
  override def perform(context: Context) {
    context.setLet(let, args(0).report(context))
    context.ip = next
  }
}
