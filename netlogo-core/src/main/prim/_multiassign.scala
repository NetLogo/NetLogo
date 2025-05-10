// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import scala.collection.mutable.Map

import org.nlogo.api.Dump
import org.nlogo.core.{ I18N, LogoList }
import org.nlogo.nvm.{ Context, Command, RuntimePrimitiveException, Workspace }

case class NestException(list: LogoList) extends Throwable

private class NestedListIter(private var list: LogoList) {
  // this functions as a stack, but Stack was deprecated in favor of List since Scala 2.11.0 (Isaac B 2/17/25)
  private var values: List[AnyRef] = list.toList

  def next(): AnyRef = {
    val value = values.head
    values = values.tail
    value
  }

  def nest(totalNeeded: Int): Unit = {
    values.head match {
      case l: LogoList =>
        if (l.size < totalNeeded)
          throw NestException(l)

        values = l.toList.take(totalNeeded) ++ values.tail

      case _ => // error, expected list
    }
  }

  def reset(l: LogoList) = {
    list = l
    values = list.toList
  }
}

object MultiAssign {
  // While there can only ever be a single multi-assignment happening at a time, because of BehaviorSpace we can have
  // multiple across workspaces so we track them this way.  -Jeremy B February 2024
  private val currentLists: Map[Workspace, NestedListIter] = Map()

  def setCurrentList(workspace: Workspace, list: LogoList): Unit = {
    if (MultiAssign.currentLists.contains(workspace)) {
      val iter = MultiAssign.currentLists.getOrElse(workspace, throw new IllegalStateException("No list for multi-assign?"))
      iter.reset(list)
    } else {
      MultiAssign.currentLists.put(workspace, new NestedListIter(list))
    }
  }

  def next(workspace: Workspace): AnyRef = {
    val iter = MultiAssign.currentLists.getOrElse(workspace,
      throw new IllegalStateException("We're trying to get a list value for a multi-assign (let or set), but the list hasn't been set?")
    )
    iter.next()
  }

  def nest(workspace: Workspace, totalNeeded: Int): Unit = {
    val iter = MultiAssign.currentLists.getOrElse(workspace,
      throw new IllegalStateException("We're trying to get a list value for a multi-assign (let or set), but the list hasn't been set?")
    )
    iter.nest(totalNeeded)
  }
}

class _multiassign(name: String, totalNeeded: Int) extends Command {
  override def perform(context: Context): Unit = {
    val list = argEvalList(context, 0)
    perform_1(context, list)
  }

  def perform_1(context: Context, list: LogoList): Unit = {
    if (totalNeeded > list.size) {
      val message = I18N.errors.getN("compiler.MultiAssign.tooFewValues", name, totalNeeded.toString, list.size.toString, Dump.logoObject(list))
      throw new RuntimePrimitiveException(context, this, message)
    }
    MultiAssign.setCurrentList(context.job.workspace, list)
    context.ip = next
  }

}
