// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.internalapi

import org.nlogo.core.{ CompilerException, Model, Program }

case class CompiledModel(
  val model:             Model,
  val compiledWidgets:   Seq[CompiledWidget],
  val interfaceControl:  InterfaceControl,
  val compilationResult: Either[CompilerException, Program]) {
    def modelLoaded(): Unit = {
      compiledWidgets.foreach(_.modelLoaded())
    }

    def modelUnloaded(): Unit = {
      compiledWidgets.foreach(_.modelUnloaded())
      interfaceControl.clearAll()
    }
  }

trait InterfaceControl {
  def notifyUpdate(update: ModelUpdate): Unit
  def clearAll(): Unit
}

object EmptyRunnableModel extends InterfaceControl {
  def notifyUpdate(update: ModelUpdate): Unit = {}
  def modelLoaded(): Unit = {}
  def modelUnloaded(): Unit = {}
  def clearAll(): Unit = {}
}
