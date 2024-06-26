// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.{ Component, FileDialog }
import java.awt.event.ActionEvent
import java.io.{ File, IOException }
import javax.swing.{ Action, AbstractAction }

import org.nlogo.api.FileIO
import org.nlogo.app.common.{ Dialogs, Events => AppEvents, TabsInterface }
import org.nlogo.awt.UserCancelException
import org.nlogo.core.I18N
import org.nlogo.ide.FocusedOnlyAction
import org.nlogo.swing.{ FileDialog => SwingFileDialog, ToolBarActionButton }
import org.nlogo.window.{ Events => WindowEvents, ExternalFileInterface }
import org.nlogo.workspace.{ AbstractWorkspace, ModelTracker }

import scala.io.Codec
import scala.util.control.Exception.ignoring
import scala.util.matching.Regex

object TemporaryCodeTab {
  private[app] def stripPath(filename: String): String = filename.split(Regex.quote(File.separator)).last
}

class TemporaryCodeTab(workspace: AbstractWorkspace with ModelTracker,
  tabs:                           TabsInterface,
  private var _filename:          TabsInterface.Filename,
  externalFileManager:            ExternalFileManager,
  conversionAction:               TemporaryCodeTab => Action,
  smartIndent:                    Boolean)
  extends CodeTab(workspace, tabs)
  with AppEvents.IndenterChangedEvent.Handler {

  var closing = false
  var saveNeeded = false // Has the buffer changed since the file was saved?

  def filename: Either[String, String] = _filename

  def filename_=(newName: Either[String, String]): Unit = {
    val oldFilename = _filename
    _filename = newName
    if (oldFilename.isRight && oldFilename != newName) {
      externalFileManager.nameChanged(oldFilename.right.get, newName.right.get)
    } else if (oldFilename.isLeft) {
      externalFileManager.add(this)
    }
  }

  def reload: Boolean = {
    var loaded: Boolean = false

    filename.right foreach { path =>
      try {
        innerSource = FileIO.fileToString(path)(Codec.UTF8).replaceAll("\r\n", "\n")
        dirty = false // Has the buffer changed since it was compiled?
        saveNeeded = false
        loaded = true
      } catch {
        case _: IOException => innerSource = ""
      }
    }

    loaded
  }

  // We're actually loading _filename for the first time here.
  if (reload) {
    externalFileManager.add(this)
  }

  setIndenter(smartIndent)
  lineNumbersVisible = tabs.lineNumbersVisible

  override val activeMenuActions = {
    Seq(undoAction, redoAction) ++
      editorConfiguration.contextActions.filter(_.isInstanceOf[FocusedOnlyAction]) ++
      filename.fold(_ => Seq(), name => Seq(conversionAction(this)))
  }

  override def getAdditionalToolBarComponents: Seq[Component] = Seq(new ToolBarActionButton(CloseAction))

  override def dirty_=(d: Boolean) = {
    super.dirty_=(d)
    if (d) {
      saveNeeded = true
      // This call lets the DirtyMonitor know if there is a separated code window
      tabs.setDirtyMonitorCodeWindow

      new WindowEvents.DirtyEvent(Some(filename.merge)).raise(this)
    }
  }

  def filenameForDisplay = (filename.right map TemporaryCodeTab.stripPath).merge

  def save(saveAs: Boolean) = {
    if (saveAs || filename.isLeft)
      filename = Right(userChooseSavePath())
    FileIO.writeFile(filename.right.get, text.getText)
    saveNeeded = false
    compileIfDirty()
    dirty = false
    // This call lets the DirtyMonitor know if there is a separated code window
    tabs.setDirtyMonitorCodeWindow

    new WindowEvents.ExternalFileSavedEvent(filename.merge).raise(this)
  }

  def close() {
    ignoring(classOf[UserCancelException]) {
      if (saveNeeded) {
        if (Dialogs.userWantsToSaveFirst(filenameForDisplay, this)) {
          // The user is saving the file with its current name
          save(false)
        }
      }
      // Remove the file from the map from file names to TemporaryCodeTabs
      externalFileManager.remove(this)
      closing = true
      // Remove from the set of TemporaryCodeTabs in Tabs and remove the tab from the JTabbedPane
      tabs.closeExternalFile(filename)
      compile()
    }
  }

  def compileIfDirty() : Unit = {
    if (dirty) {
      compile()
    }
  }

  override def handle(e: WindowEvents.CompiledEvent) = {
    def setErrorLabel() = errorLabel.setError(e.error, e.sourceOwner.headerSource.size)

    dirty = false
    e.sourceOwner match {
      case file: ExternalFileInterface if file.getFileName == filename.right.get => setErrorLabel()
      // if the Code tab compiles then get rid of the error ev 7/26/07
      case tab: CodeTab if e.error == null                                       => setErrorLabel()
      case _ =>
    }
  }

  override def handle(e: AppEvents.SwitchedTabsEvent) = if (!closing) super.handle(e)

  final def handle(e: AppEvents.IndenterChangedEvent) = setIndenter(e.isSmart)

  private def userChooseSavePath(): String = {
    def appendIfNecessary(str: String, suffix: String) = if (str.endsWith(suffix)) str else str + suffix

    val newFileName = appendIfNecessary(filenameForDisplay, ".nls")
    val path = SwingFileDialog.showFiles(this, I18N.gui.get("file.save.external"), FileDialog.SAVE, newFileName)
    appendIfNecessary(path, ".nls")
  }

  private object CloseAction extends AbstractAction(I18N.gui.get("tabs.external.close")) {
    override def actionPerformed(e: ActionEvent) = close()
  }
}
