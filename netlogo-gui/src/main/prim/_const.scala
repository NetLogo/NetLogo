// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Dump
import org.nlogo.core.Pure
import org.nlogo.nvm.{ Context, Reporter }

class _const(_value: AnyRef) extends Reporter with Pure {

  def value = _value

  // readable = true so we can distinguish e.g. the number 2 from the string "2"
  override def toString =
    s"${super.toString}:${Dump.logoObject(value, readable = true, exporting = false)}"

  override def report(context: Context): AnyRef =
    report_1(context)

  def report_1(context: Context): AnyRef =
    _value

}
