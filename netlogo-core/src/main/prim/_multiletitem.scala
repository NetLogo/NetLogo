// // (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Dump
import org.nlogo.core.LogoList
import org.nlogo.nvm.{ Context, Reporter, RuntimePrimitiveException }

class _multiletitem(private[this] val index: Int, private[this] val totalNeeded: Int) extends Reporter {
  override def report(context: Context): AnyRef = {
    val list = argEvalList(context, 0)
    report_1(context, list)
  }

  def report_1(context: Context, list: LogoList): AnyRef = {
    if (totalNeeded > list.size) {
      val message = s"The list of values for LET must be at least as long as the list of names.  We need $totalNeeded value(s) but only got ${list.size} from the list ${Dump.logoObject(list)}."
      throw new RuntimePrimitiveException(context, this, message)
    }
    list.get(index)
  }

}
