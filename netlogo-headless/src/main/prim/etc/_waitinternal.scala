// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Let
import org.nlogo.nvm.{ Command, Context, MutableLong }

class _waitinternal(let: Let) extends Command {
  switches = true
  override def perform(context: Context): Unit = {
    perform_1(context)
  }
  def perform_1(context: Context): Unit = {
    if (System.nanoTime >= context.activation.binding.getLet(let).asInstanceOf[MutableLong].value)
      context.ip = next
  }
}
