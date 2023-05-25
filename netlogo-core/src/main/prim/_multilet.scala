// // (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Dump
import org.nlogo.core.LogoList
import org.nlogo.nvm.{ Context, Command, RuntimePrimitiveException }

object MultiLet {
  var currentList: Option[LogoList] = None

  def get(index: Int): AnyRef = {
    MultiLet.currentList.getOrElse(
      throw new IllegalStateException("We're trying to get a list value for a multilet, but the list hasn't been set?")
    ).get(index)
  }
}

class _multilet(private[this] val totalNeeded: Int) extends Command {
  override def perform(context: Context): Unit = {
    val list = argEvalList(context, 0)
    perform_1(context, list)
  }

  def perform_1(context: Context, list: LogoList): Unit = {
    if (totalNeeded > list.size) {
      val message = s"The list of values for LET must be at least as long as the list of names.  We need $totalNeeded value(s) but only got ${list.size} from the list ${Dump.logoObject(list)}."
      throw new RuntimePrimitiveException(context, this, message)
    }
    MultiLet.currentList = Some(list)
    context.ip = next
  }

}
