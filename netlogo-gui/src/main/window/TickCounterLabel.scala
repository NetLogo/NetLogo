// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Dimension

import javax.swing.JLabel

import org.nlogo.api.{ Dump, World }
import org.nlogo.window.Events.{ PeriodicUpdateEvent, LoadBeginEvent, LoadEndEvent }

object TickCounterLabel {
  private val TickCounterLabelDefault = "ticks"
}

import TickCounterLabel._

class TickCounterLabel(world: World)
  extends JLabel
  with PeriodicUpdateEvent.Handler
  with LoadBeginEvent.Handler {
  private var _label: String = TickCounterLabelDefault

  override def getPreferredSize: Dimension = getMinimumSize

  override def getMinimumSize: Dimension = {
    val d = super.getMinimumSize
    val fontMetrics = getFontMetrics(getFont)
    d.width = StrictMath.max(d.width, fontMetrics.stringWidth(label + ": 00000000"))
    d
  }

  def handle(e: LoadBeginEvent): Unit = {
    setText("")
    _label = "ticks"
    setVisible(true)
  }

  def handle(e: PeriodicUpdateEvent): Unit = {
    redrawTickCounter()
  }

  protected def redrawTickCounter(): Unit = {
    val ticks = world.ticks
    val tickText =
        if (ticks == -1) "" else Dump.number(StrictMath.floor(ticks))
    setText("     " + _label + ": " + tickText)
  }

  /// tick counter

  def visibility_=(visible: Boolean): Unit =
    setVisible(visible)

  def visibility: Boolean = isVisible

  def label_=(label: String): Unit = {
    _label = label
    redrawTickCounter()
  }

  def label: String = _label

}
