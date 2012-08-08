// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }

class _setprocedurevariable(vn: Int, name: String) extends Command {
  def this(original: _procedurevariable) =
    this(original.vn, original.name)
  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.WildcardType))
  override def toString =
    super.toString + ":" + name
  override def perform(context: Context) {
    perform_1(context, args(0).report(context))
  }
  def perform_1(context: Context, arg0: AnyRef) {
    context.activation.args(vn) = arg0
    context.ip = next
  }
}
