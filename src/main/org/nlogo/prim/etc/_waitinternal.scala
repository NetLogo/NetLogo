// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context, MutableLong }
import org.nlogo.api.{ Let, Syntax }

class _waitinternal(let: Let) extends Command {
  override def syntax =
    Syntax.commandSyntax(true)
  override def perform(context: Context) {
    perform_1(context)
  }
  def perform_1(context: Context) {
    if (System.nanoTime >= context.getLet(let).asInstanceOf[MutableLong].value)
      context.ip = next
  }
}
