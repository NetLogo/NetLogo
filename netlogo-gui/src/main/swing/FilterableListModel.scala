// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import javax.swing.{ AbstractListModel, ListModel }
import javax.swing.event.{ ListDataEvent, ListDataListener }

import scala.collection.mutable

class FilterableListModel[T](underlying: ListModel[T], predicate: (T, String) => Boolean)
extends AbstractListModel[T] {
  private val originalList = {
    val arr = new mutable.ArrayBuffer[T](underlying.getSize)
    (0 until underlying.getSize) foreach { arr += underlying.getElementAt(_) }
    arr
  }
  private var filteredList = originalList.clone
  private var filterText = ""
  private var filtering = false

  underlying.addListDataListener(new ListDataListener {
    def intervalAdded(e: ListDataEvent)   = {
      (e.getIndex0 to e.getIndex1) foreach { i => originalList.insert(i, underlying.getElementAt(i)) }
      refilter()
    }
    def intervalRemoved(e: ListDataEvent) = {
      originalList.remove(e.getIndex0, e.getIndex1 - e.getIndex0 + 1)
      refilter()
    }
    def contentsChanged(e: ListDataEvent) = {
      (e.getIndex0 to e.getIndex1) foreach { i => originalList(i) = underlying.getElementAt(i) }
      refilter()
    }
  })

  def filter(text: String) = {
    filterText = text
    filtering = true
    if (filteredList.length > 0)
      fireIntervalRemoved(this, 0, filteredList.length - 1)
    filteredList = originalList.filter(predicate(_, text))
    filtering = false
    if (filteredList.length > 0)
      fireIntervalAdded(this, 0, filteredList.length - 1)
  }

  def refilter() = filter(filterText)

  override def getSize = if (filtering) 0 else filteredList.length
  override def getElementAt(i: Int) = filteredList(i)
}
