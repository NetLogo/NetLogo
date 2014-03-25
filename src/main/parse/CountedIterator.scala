// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

class CountedIterator[T](it: Iterator[T]) extends Iterator[T] {
  private var _count = 0
  def count = _count
  def next() = { _count += 1; it.next() }
  def hasNext = it.hasNext
}
