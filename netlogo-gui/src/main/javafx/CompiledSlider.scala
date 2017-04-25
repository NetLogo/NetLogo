// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.javafx

import
  org.nlogo.{ core, internalapi },
    core.{ AgentKind, Slider => CoreSlider },
    internalapi.{ CompiledSlider => ApiCompiledSlider, Monitorable, UpdateVariable }

case class CompiledSlider(
  val widget: CoreSlider,
  val value:  Monitorable[Double] with ReporterMonitorable,
  val min:    Monitorable[Double],
  val max:    Monitorable[Double],
  val inc:    Monitorable[Double],
  widgetActions: WidgetActions) extends ApiCompiledSlider {

  def setValue(update: Double): Unit = {
    for {
      name <- widget.variable
    } {
      widgetActions.runOperation(
        UpdateVariable(name, AgentKind.Observer, 0,
          Double.box(value.currentValue),
          Double.box(update)), value)
    }
  }

  override def modelLoaded(): Unit = {
    Seq(value, min, max, inc).foreach {
      case c: CompiledMonitorable[Double] => widgetActions.addMonitorable(c)
      case _ =>
    }
  }
}
