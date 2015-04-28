package org.nlogo.app.previewcommands

import scala.util.Failure
import scala.util.Success
import scala.util.Try

import org.nlogo.api.CompilerException
import org.nlogo.api.PreviewCommands
import org.nlogo.api.PreviewCommands.Compilable
import org.nlogo.nvm.RunnablePreviewCommands
import org.nlogo.nvm.WorkspaceFactory
import org.nlogo.swing.HasPropertyChangeSupport
import org.nlogo.workspace.AbstractWorkspaceScala

class GUIState(
  val modelContents: String,
  ws: AbstractWorkspaceScala,
  workspaceFactory: WorkspaceFactory)
  extends HasPropertyChangeSupport {

  private var _previewCommands: PreviewCommands = null
  def previewCommands = _previewCommands

  private var _compilerException: Option[CompilerException] = null

  def compilablePreviewCommands: Option[PreviewCommands.Compilable] =
    Option(_previewCommands).collect { case cc: Compilable => cc }
  def runnablePreviewCommands: Option[RunnablePreviewCommands] =
    compilablePreviewCommands
      .filterNot(_ => _compilerException.isDefined)
      .map(new RunnablePreviewCommands(_, modelContents, workspaceFactory))
  def previewCommands_=(newPreviewCommands: PreviewCommands): Unit = {
    val oldPreviewCommands = _previewCommands
    _previewCommands = newPreviewCommands
    val oldCompilerException = _compilerException
    _compilerException = compilablePreviewCommands.flatMap { commands =>
      Try(ws.compileCommands(commands.source)) match {
        case Success(_) => None
        case Failure(e: CompilerException) => Some(e)
        case Failure(e) => throw e
      }
    }
    propertyChangeSupport.firePropertyChange("previewCommands", oldPreviewCommands, _previewCommands)
    propertyChangeSupport.firePropertyChange("compilerException", oldCompilerException, _compilerException)
  }
}
