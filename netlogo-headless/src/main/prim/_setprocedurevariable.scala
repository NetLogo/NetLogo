// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.nvm.{ Command, Context }

class _setprocedurevariable(vn: Int, name: String) extends Command {
  def this(original: _procedurevariable) =
    this(original.vn, original.name)

  override def toString =
    super.toString + ":" + name
  override def perform(context: Context): Unit = {
    perform_1(context, args(0).report(context))
  }
  def perform_1(context: Context, arg0: AnyRef): Unit = {
    context.activation.args(vn) = arg0
    context.ip = next
  }
}
