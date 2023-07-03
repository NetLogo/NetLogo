// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.{ Component, FileDialog }
import java.awt.event.ActionEvent
import java.io.{ File, IOException }
import javax.swing.{ Action, AbstractAction }

import org.nlogo.api.FileIO
import org.nlogo.app.common.{ Actions, Dialogs, Events => AppEvents, ExceptionCatchingAction, TabsInterface },
  Actions.Ellipsis
import org.nlogo.awt.UserCancelException
import org.nlogo.core.I18N
import org.nlogo.ide.FocusedOnlyAction
import org.nlogo.swing.{ FileDialog => SwingFileDialog, ToolBarActionButton, UserAction },
  UserAction.MenuAction
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
  var saveNeeded = false

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

  filename.right foreach { path =>
    try {
      innerSource = FileIO.fileToString(path)(Codec.UTF8).replaceAll("\r\n", "\n")
      saveNeeded = false
      externalFileManager.add(this)
      // This compilation should act as a failsafe in case the file changed on
      // disk since the model was opened. AAB 03 2022
      compile()
      dirty = false
    } catch {
      case _: IOException => innerSource = ""
    }
  }
  setIndenter(smartIndent)
  lineNumbersVisible = tabs.lineNumbersVisible

  override val activeMenuActions = {
    def saveAction(saveAs: Boolean) = {
      new ExceptionCatchingAction(if (saveAs) I18N.gui.get("menu.file.saveAs") + Ellipsis else I18N.gui.get("menu.file.save"), TemporaryCodeTab.this)
      with MenuAction {
        category    = UserAction.FileCategory
        group       = UserAction.FileSaveGroup
        accelerator = UserAction.KeyBindings.keystroke('S', withMenu = true, withShift = saveAs)
        rank = 0

        @throws(classOf[UserCancelException])
        override def action(): Unit = {
          save(saveAs)
        }
      }
    }
    Seq(saveAction(false), saveAction(true), undoAction, redoAction) ++
      editorConfiguration.contextActions.filter(_.isInstanceOf[FocusedOnlyAction]) ++
      filename.fold(_ => Seq(), name => Seq(conversionAction(this)))
  }

  override def getAdditionalToolBarComponents: Seq[Component] = Seq(new ToolBarActionButton(CloseAction))

  override def dirty_=(d: Boolean) = {
    super.dirty_=(d)
    if (d) {
      saveNeeded = true
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
    tabs.setDirtyMonitorCodeWindow
    new WindowEvents.ExternalFileSavedEvent(filename.merge).raise(this)
  }

  def close() {
    var compileNeeded = false
    ignoring(classOf[UserCancelException]) {
      if(saveNeeded) {
        if (Dialogs.userWantsToSaveFirst(filenameForDisplay, this)) {
          save(false)
          compile()
        } else {
          // If the user doesn't save the buffer and it was dirty, the file should
          // be compiled after it is closed AAB 03-2022
          compileNeeded = true
        }
      }
      externalFileManager.remove(this)
      closing = true
      tabs.closeExternalFile(filename)
      if (compileNeeded) {
        new WindowEvents.CompileAllEvent().raiseLater(this)
      }
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
