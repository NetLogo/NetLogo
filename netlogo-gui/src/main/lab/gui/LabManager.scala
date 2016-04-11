// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import org.nlogo.api.ModelSection
import org.nlogo.lab.{Protocol, ProtocolLoader, ProtocolSaver}
import org.nlogo.window.{GUIWorkspace, EditDialogFactoryInterface, LabManagerInterface}
import org.nlogo.workspace.{CurrentModelOpener, WorkspaceFactory}
import org.nlogo.window.Events._
import scala.collection.mutable.ListBuffer

class LabManager(val workspace: GUIWorkspace,
                 dialogFactory: EditDialogFactoryInterface,
                 val workspaceFactory: WorkspaceFactory with CurrentModelOpener)
  extends LabManagerInterface
  with CompiledEvent.Handler
  with LoadBeginEvent.Handler
  with LoadSectionEvent.Handler
{
  val protocols = new ListBuffer[Protocol]
  private lazy val dialog = new ManagerDialog(this, dialogFactory)
  def show() { dialog.update(); dialog.setVisible(true) }
  def close() { dialog.setVisible(false) }
  def dirty() { new DirtyEvent().raise(this) }
  /// Event.LinkChild -- lets us get events out to rest of app
  val getLinkParent = workspace
  /// loading & saving
  def handle(e:LoadBeginEvent) {
    close()
    protocols.clear()
    lastCompileAllWasSuccessful = false
  }
  def handle(e:LoadSectionEvent) {
    // autoconversion of protocols from old NetLogo versions
    def autoConvert(protocol:Protocol):Protocol = {
      import protocol._
      new Protocol(name,
                   workspace.autoConvert(e.version)(setupCommands),
                   workspace.autoConvert(e.version)(goCommands),
                   workspace.autoConvert(e.version)(finalCommands),
                   repetitions, runMetricsEveryStep, timeLimit,
                   workspace.autoConvert(e.version)(exitCondition),
                   metrics.map(workspace.autoConvert(e.version)),
                   valueSets)
    }
    if(e.section == ModelSection.BehaviorSpace && !e.text.trim.isEmpty)
      protocols ++= new ProtocolLoader(workspace).loadAll(e.text).map(autoConvert)
  }
  def save =
    if(protocols.isEmpty) ""
    else ProtocolSaver.save(protocols)
  /// making sure everything gets compiled before an experiment run
  private var lastCompileAllWasSuccessful = false
  def handle(e:CompiledEvent) {
    if(e.sourceOwner.isInstanceOf[org.nlogo.window.ProceduresInterface])
      lastCompileAllWasSuccessful = e.error == null
  }
  def prepareForRun() {
    (new CompileAllEvent).raise(this)
    if(!lastCompileAllWasSuccessful)
      throw new org.nlogo.awt.UserCancelException
  }
}
