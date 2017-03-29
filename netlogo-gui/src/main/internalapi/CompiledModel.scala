// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.internalapi

import org.nlogo.core.{ Button, Model, CompilerException, Program, Widget }

sealed trait CompiledWidget {
  def widget: Widget
  def compilerError: Option[CompilerException]
}

trait CompiledButton extends CompiledWidget {
  def widget: Button
  def procedureTag: String
}

case class NonCompiledWidget(val widget: Widget) extends CompiledWidget {
  def compilerError = None
}

case class CompiledModel(
  val model: Model,
  val compiledWidgets: Seq[CompiledWidget],
  val runnableModel: RunnableModel,
  val compilationResult: Either[CompilerException, Program])

trait ModelRunner {
  def tagError(tag: String, error: Exception): Unit
}

trait RunComponent {
  def tagAction(action: ModelAction, actionTag: String): Unit
  def updateReceived(update: ModelUpdate): Unit
}

trait RunnableModel {
  def submitAction(action: ModelAction): Unit // when you don't care that the job completes
  def submitAction(action: ModelAction, component: RunComponent): Unit
  def notifyUpdate(update: ModelUpdate): Unit
}

object EmptyRunnableModel extends RunnableModel {
  def submitAction(action: ModelAction): Unit = {}
  def submitAction(action: ModelAction, component: RunComponent): Unit = {
    component.tagAction(action, "")
    component.updateReceived(ActionCompleted(""))
  }
  def notifyUpdate(update: ModelUpdate): Unit = {}
}
