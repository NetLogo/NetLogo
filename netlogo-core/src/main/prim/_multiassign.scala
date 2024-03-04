// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import scala.collection.mutable.Map

import org.nlogo.api.Dump
import org.nlogo.core.LogoList
import org.nlogo.nvm.{ Context, Command, RuntimePrimitiveException, Workspace }

private class SimpleListIter(private var list: LogoList) {
  private var nextIndex = 0
  def next(): AnyRef = {
    val value = list.get(nextIndex)
    nextIndex = nextIndex + 1
    value
  }

  def reset(l: LogoList) = {
    list = l
    nextIndex = 0
  }
}

object MultiAssign {
  // While there can only ever be a single multi-assignment happening at a time, because of BehaviorSpace we can have
  // multiple across workspaces so we track them this way.  -Jeremy B February 2024
  private val currentLists: Map[Workspace, SimpleListIter] = Map()

  def setCurrentList(workspace: Workspace, list: LogoList) {
    if (MultiAssign.currentLists.contains(workspace)) {
      val iter = MultiAssign.currentLists.getOrElse(workspace, throw new IllegalStateException("No list for multi-assign?"))
      iter.reset(list)
    } else {
      MultiAssign.currentLists.put(workspace, new SimpleListIter(list))
    }
  }

  def next(workspace: Workspace): AnyRef = {
    val iter = MultiAssign.currentLists.getOrElse(workspace,
      throw new IllegalStateException("We're trying to get a list value for a multi-assign (let or set), but the list hasn't been set?")
    )
    iter.next()
  }
}

class _multiassign(private[this] val name: String, private[this] val totalNeeded: Int) extends Command {
  override def perform(context: Context): Unit = {
    val list = argEvalList(context, 0)
    perform_1(context, list)
  }

  def perform_1(context: Context, list: LogoList): Unit = {
    if (totalNeeded > list.size) {
      val message = s"The list of values for $name must be at least as long as the list of names.  We need $totalNeeded value(s) but only got ${list.size} from the list ${Dump.logoObject(list)}."
      throw new RuntimePrimitiveException(context, this, message)
    }
    MultiAssign.setCurrentList(context.job.workspace, list)
    context.ip = next
  }

}
