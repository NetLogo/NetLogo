// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Dump
import org.nlogo.core.LogoList
import org.nlogo.nvm.{ Context, Command, RuntimePrimitiveException }

object MultiLet {
  private var currentList: Option[LogoList] = None
  private var nextIndex: Int = -1

  def setCurrentList(list: LogoList) {
    MultiLet.currentList = Some(list)
    MultiLet.nextIndex   = 0
  }

  def next(): AnyRef = {
    val value = MultiLet.currentList.getOrElse(
      throw new IllegalStateException("We're trying to get a list value for a multi-bind (let or set), but the list hasn't been set?")
    ).get(nextIndex)
    nextIndex = nextIndex + 1
    value
  }
}

class _multilet(private[this] val name: String, private[this] val totalNeeded: Int) extends Command {
  override def perform(context: Context): Unit = {
    val list = argEvalList(context, 0)
    perform_1(context, list)
  }

  def perform_1(context: Context, list: LogoList): Unit = {
    if (totalNeeded > list.size) {
      val message = s"The list of values for $name must be at least as long as the list of names.  We need $totalNeeded value(s) but only got ${list.size} from the list ${Dump.logoObject(list)}."
      throw new RuntimePrimitiveException(context, this, message)
    }
    MultiLet.setCurrentList(list)
    context.ip = next
  }

}
