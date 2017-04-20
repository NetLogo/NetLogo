// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.internalapi

import org.nlogo.core.{ CompilerException, Model, Program }

case class CompiledModel(
  val model: Model,
  val compiledWidgets: Seq[CompiledWidget],
  val runnableModel: RunnableModel,
  val compilationResult: Either[CompilerException, Program])

// TODO: This is a TERRIBLE name
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
