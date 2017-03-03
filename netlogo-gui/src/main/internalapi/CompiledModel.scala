// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.internalapi

import org.nlogo.core.{ Button, Model, CompilerException, Program, Widget }

sealed trait CompiledWidget {
  def widget: Widget
  def compilerError: Option[CompilerException]
}
case class CompiledButton(val widget: Button, val compilerError: Option[CompilerException], val tag: String) extends CompiledWidget
case class NonCompiledWidget(val widget: Widget) extends CompiledWidget {
  def compilerError = None
}
case class CompiledModel(
  val model: Model,
  val compiledWidgets: Seq[CompiledWidget],
  val compilationResult: Either[CompilerException, Program])
