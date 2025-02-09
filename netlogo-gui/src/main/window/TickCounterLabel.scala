// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.JLabel

import org.nlogo.api.{ Dump, World }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.Events.{ AfterLoadEvent, PeriodicUpdateEvent, LoadBeginEvent }

object TickCounterLabel {
  private val TickCounterLabelDefault = "ticks"
}

import TickCounterLabel._

class TickCounterLabel(world: World)
  extends JLabel
  with AfterLoadEvent.Handler
  with LoadBeginEvent.Handler
  with PeriodicUpdateEvent.Handler
  with ThemeSync {
  private var _label: String = TickCounterLabelDefault

  def handle(e: LoadBeginEvent): Unit = {
    setText("")
    _label = "ticks"
    setVisible(true)
  }

  def handle(e: AfterLoadEvent): Unit = {
    redrawTickCounter()
  }

  def handle(e: PeriodicUpdateEvent): Unit = {
    redrawTickCounter()
  }

  protected def redrawTickCounter(): Unit = {
    val ticks = world.ticks
    val tickText =
        if (ticks == -1) "" else Dump.number(StrictMath.floor(ticks))
    setText((_label + ": " + tickText).trim)
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

  override def syncTheme(): Unit = {
    setForeground(InterfaceColors.TOOLBAR_TEXT)
  }
}
