// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import scala.util.{ Failure, Success, Try }

import org.nlogo.core.{ CompilerException, Model }
import org.nlogo.api.PreviewCommands
import org.nlogo.swing.HasPropertyChangeSupport
import org.nlogo.workspace.{ PreviewCommandsRunner, WorkspaceFactory }

class GUIState(
  model: Model,
  modelPath: String,
  workspaceFactory: WorkspaceFactory)
  extends HasPropertyChangeSupport {

  private var _previewCommands: Option[PreviewCommands] = None
  private var _previewCommandsRunner: Option[Either[CompilerException, PreviewCommandsRunner]] = None

  def previewCommands: Option[PreviewCommands] =
    _previewCommands
  def previewCommandsRunnable: Option[PreviewCommandsRunner#Runnable] =
    _previewCommandsRunner.flatMap(_.toOption.map(_.runnable))
  def compilerException: Option[CompilerException] =
    _previewCommandsRunner.flatMap(_.left.toOption)

  def previewCommands_=(newPreviewCommands: PreviewCommands): Unit = {
    val oldPreviewCommands = previewCommands
    val oldCompilerException = compilerException
    _previewCommands = Some(newPreviewCommands)
    _previewCommandsRunner = newPreviewCommands match {
      case commands: PreviewCommands if commands.compilable =>
        Try(PreviewCommandsRunner.fromModelContents(
          workspaceFactory, model, modelPath, newPreviewCommands
        )) match {
          case Success(runner)               => Some(Right(runner))
          case Failure(e: CompilerException) => Some(Left(e))
          case Failure(e)                    => throw e
        }
      case _ => None
    }
    propertyChangeSupport.firePropertyChange("previewCommands", oldPreviewCommands, previewCommands)
    propertyChangeSupport.firePropertyChange("compilerException", oldCompilerException, compilerException)
  }

}
