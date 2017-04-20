// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.internalapi

import org.nlogo.core.{ Button, CompilerException, Monitor, Slider, Widget }

sealed trait CompiledWidget {
  def widget: Widget
}

trait CompiledButton extends CompiledWidget {
  def widget: Button
  def procedureTag: String
  def compilerError: Option[CompilerException]
  def ticksEnabled: Monitorable[Boolean]
}

// consider adding an onError callback
trait CompiledMonitor extends CompiledWidget with Monitorable[String] {
  def widget: Monitor
}

trait CompiledSlider extends CompiledWidget {
  def widget: Slider
  def value: Monitorable[Double]
  def min:   Monitorable[Double]
  def max:   Monitorable[Double]
  def inc:   Monitorable[Double]
}

case class NonCompiledWidget(val widget: Widget) extends CompiledWidget {
  def compilerError = None
}
