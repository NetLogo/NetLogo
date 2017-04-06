// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.internalapi

import org.nlogo.core.{ Button, CompilerException, Model, Monitor, Program, Slider, Widget }

sealed trait CompiledWidget {
  def widget: Widget
}

trait CompiledButton extends CompiledWidget {
  def widget: Button
  def procedureTag: String
  def compilerError: Option[CompilerException]
}

// maybe make this generic in the callback argument
trait Monitorable[A] {
  // this callback will be called on the JavaFX UI thread when the value changes
  def onUpdate(callback: A => Unit): Unit
  def compilerError: Option[CompilerException]
  def defaultValue: A
  def procedureTag: String
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

case class CompiledModel(
  val model: Model,
  val compiledWidgets: Seq[CompiledWidget],
  val runnableModel: RunnableModel,
  val compilationResult: Either[CompilerException, Program])

//TODO: This is a TERRIBLE name
trait RunComponent {
  def tagAction(action: ModelAction, actionTag: String): Unit
  def updateReceived(update: ModelUpdate): Unit
}

trait RunnableModel {
  def submitAction(action: ModelAction): Unit // when you don't care that the job completes
  def submitAction(action: ModelAction, component: RunComponent): Unit
  def notifyUpdate(update: ModelUpdate): Unit
  def modelLoaded(): Unit
  def modelUnloaded(): Unit
}

object EmptyRunnableModel extends RunnableModel {
  def submitAction(action: ModelAction): Unit = {}
  def submitAction(action: ModelAction, component: RunComponent): Unit = {
    component.tagAction(action, "")
    component.updateReceived(JobDone(""))
  }
  def notifyUpdate(update: ModelUpdate): Unit = {}
  def modelLoaded(): Unit = {}
  def modelUnloaded(): Unit = {}
}
