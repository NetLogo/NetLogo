// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.awt.event.ActionEvent
import javax.swing.AbstractAction

import org.nlogo.api.{ GenericModelLoader, LabProtocol }
import org.nlogo.awt.Positioning
import org.nlogo.core.{ I18N, Model }
import org.nlogo.window.{ GUIWorkspace, EditDialogFactoryInterface, LabManagerInterface, MenuBarFactory }
import org.nlogo.window.Events._
import org.nlogo.workspace.{ CurrentModelOpener, WorkspaceFactory }
import org.nlogo.swing.UserAction.{ ToolsCategory, ToolsDialogsGroup, KeyBindings, MenuAction }

import scala.collection.mutable.ListBuffer

class LabManager(val workspace:        GUIWorkspace,
                 dialogFactory:        EditDialogFactoryInterface,
                 menuFactory:          MenuBarFactory,
                 val workspaceFactory: WorkspaceFactory with CurrentModelOpener,
                 val modelLoader:      GenericModelLoader)
  extends LabManagerInterface
  with CompiledEvent.Handler
  with LoadBeginEvent.Handler
  with LoadModelEvent.Handler
{
  val protocols = new ListBuffer[LabProtocol]

  def getComponent: Seq[LabProtocol] = protocols.toSeq
  def defaultComponent: Seq[LabProtocol] = Seq()

  def addProtocol(p: LabProtocol): Unit = {
    protocols += p
  }

  def clearProtocols(): Unit = {
    protocols.clear()
  }
  private lazy val dialog = new ManagerDialog(this, dialogFactory, menuFactory)
  def show() {
    Positioning.center(dialog, workspace.getFrame)
    dialog.update()
    dialog.setVisible(true)
  }
  def close() {
    dialogFactory.clearDialog()
    dialog.setVisible(false)
  }
  def dirty() { new DirtyEvent(None).raise(this) }
  /// Event.LinkChild -- lets us get events out to rest of app
  val getLinkParent = workspace
  /// loading & saving
  def handle(e:LoadBeginEvent) {
    close()
    protocols.clear()
    lastCompileAllWasSuccessful = false
  }
  def handle(e:LoadModelEvent) {
    protocols ++= e.model
      .optionalSectionValue[Seq[LabProtocol]]("org.nlogo.modelsection.behaviorspace")
      .getOrElse(Seq[LabProtocol]())
    workspace.setBehaviorSpaceExperiments(protocols.toList)
  }
  override def updateModel(m: Model): Model =
    m.withOptionalSection("org.nlogo.modelsection.behaviorspace", Some(protocols), Seq())

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

  val actions = Seq(new ShowLabManager)

  class ShowLabManager extends AbstractAction(I18N.gui.get(s"menu.tools.behaviorSpace")) with MenuAction {
    category    = ToolsCategory
    group       = ToolsDialogsGroup
    accelerator = KeyBindings.keystroke('B', withMenu = true, withShift = true)

    override def actionPerformed(e: ActionEvent) {
      show()
    }
  }
}
