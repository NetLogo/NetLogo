// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.core.{ CompilerException, Model, Program }
import org.nlogo.api.{ JobOwner, ModelType }
import org.nlogo.nvm.{ Context, Instruction }

import scala.reflect.ClassTag
import scala.collection.mutable.{ Publisher, Subscriber }

sealed trait WorkspaceEvent

case class ModelPathChanged(modelFileName: Option[String], modelDirName: Option[String]) extends WorkspaceEvent
case class ModelCompiledSuccess(program: Program) extends WorkspaceEvent
case class ModelCompiledFailure(error: CompilerException) extends WorkspaceEvent
case class ToggleCompilerTesting(isOn: Boolean) extends WorkspaceEvent
case class WarningMessage(text: String) extends WorkspaceEvent
case class RuntimeError(owner: JobOwner, context: Context, instruction: Instruction, exception: Exception) extends WorkspaceEvent
case class AddInstrumentation[A : ClassTag](name: String, instrument: A, classTag: ClassTag[A]) extends WorkspaceEvent
case class RemoveInstrumentation[A : ClassTag](name: String, classTag: ClassTag[A]) extends WorkspaceEvent
case class SwitchModel(modelPath: Option[String], modelType: ModelType) extends WorkspaceEvent
case class LoadModel(model: Model) extends WorkspaceEvent

trait WorkspaceMessageListener extends Subscriber[WorkspaceEvent, WorkspaceMessageCenter#Pub] {
  def processWorkspaceEvent(evt: WorkspaceEvent): Unit

  def notify(publisher: WorkspaceMessageCenter#Pub, evt: WorkspaceEvent): Unit = {
    processWorkspaceEvent(evt)
  }
}

class WorkspaceMessageCenter extends Publisher[WorkspaceEvent] {
  def send(evt: WorkspaceEvent): Unit = {
    publish(evt)
  }
}
