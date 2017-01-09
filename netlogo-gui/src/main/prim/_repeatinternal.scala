// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.{ Let, Syntax }
import org.nlogo.nvm.{ Command, Context, MutableLong }

class _repeatinternal(_offset: Int, private[this] val _let: Let) extends Command {
  offset = _offset
  def let = _let

  override def toString: String =
    super.toString + ":" + offset

  override def perform(context: Context): Unit = {
    val counter = context.activation.binding.getLet(_let).asInstanceOf[MutableLong]
    if (counter.value <= 0) {
      context.ip = next
    } else {
      counter.value = counter.value - 1
      context.ip = offset
    }
  }

  def perform_1(context: Context): Unit = {
    val counter = context.activation.binding.getLet(_let).asInstanceOf[MutableLong]
    if (counter.value <= 0) {
      context.ip = next
    } else {
      counter.value = counter.value - 1
      context.ip = offset
    }
  }
}
