// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

case class IndexedNote(
  val frame: Int,
  val ticks: Double,
  val text: String = "") extends Ordered[IndexedNote] {
  def compare(that: IndexedNote) = frame.compareTo(that.frame)
}
