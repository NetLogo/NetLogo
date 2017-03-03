// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.internalapi

import org.nlogo.core.{ Button, Model, CompilerException, Program, Widget }

sealed trait CompiledWidget {
  def widget: Widget
  def compilerError: Option[CompilerException]
}

trait CompiledButton extends CompiledWidget {
  def widget: Button
  def tag: String
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

trait RunnableModel {
  def runTag(tag: String, runner: ModelRunner): Unit
}

object EmptyRunnableModel extends RunnableModel {
  def runTag(tag: String, runner: ModelRunner): Unit = {}
}
