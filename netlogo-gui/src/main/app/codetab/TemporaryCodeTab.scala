// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.FileDialog
import java.io.IOException

import org.nlogo.api.FileIO
import org.nlogo.app.common.{ Dialogs, Events => AppEvents, TabsInterface }
import org.nlogo.core.I18N
import org.nlogo.ide.FocusedOnlyAction
import org.nlogo.swing.{ CloseableTab, FileDialog => SwingFileDialog, UserAction }
import org.nlogo.util.PathUtils
import org.nlogo.window.{ Events => WindowEvents, ExternalFileInterface }
import org.nlogo.workspace.{ AbstractWorkspace, ModelTracker }

import scala.util.matching.Regex

object TemporaryCodeTab {
  private[app] def stripPath(filename: String): String = filename.split(Regex.quote("/")).last
}

class TemporaryCodeTab(workspace: AbstractWorkspace & ModelTracker,
  tabs:                           TabsInterface,
  private var _filename:          TabsInterface.Filename,
  externalFileManager:            ExternalFileManager,
  conversionAction:               TemporaryCodeTab => UserAction.MenuAction,
  separateCodeWindow:             Boolean)
  extends CodeTab(workspace, tabs) with CloseableTab {

  var closing = false
  var saveNeeded = false // Has the buffer changed since the file was saved?

  checkCompilable(false)

  def filename: Either[String, String] = _filename

  def filename_=(newName: Either[String, String]): Unit = {
    val oldFilename = _filename
    _filename = newName.map(PathUtils.standardize)
    if (oldFilename.isRight && oldFilename != _filename) {
      externalFileManager.nameChanged(oldFilename.getOrElse(null), _filename.getOrElse(null))
    } else if (oldFilename.isLeft) {
      externalFileManager.add(this)
    }
  }

  def reload: Boolean = {
    var loaded: Boolean = false

    filename foreach { path =>
      try {
        innerSource = FileIO.fileToString(path).replaceAll("\r\n", "\n")
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

  setSeparate(separateCodeWindow)
  lineNumbersVisible = tabs.lineNumbersVisible
  setIndenter(tabs.smartTabbingEnabled)

  override val activeMenuActions = {
    Seq(undoAction, redoAction) ++
      editorConfiguration.contextActions.filter(_.isInstanceOf[FocusedOnlyAction]) ++
      filename.fold(_ => Seq(), name => Seq(conversionAction(this)))
  }

  // if included file is not saved and not referenced in the main Code tab,
  // disable Check button and show warning banner (Isaac B 6/26/25)
  private def checkCompilable(dirty: Boolean): Unit = {
    if (getIncludesTable.exists(_.exists(_._2 == filename.getOrElse(null)))) {
      compileButton.setEnabled(dirty)
      errorLabel.setWarning(None)
    } else {
      compileButton.setEnabled(false)
      errorLabel.setWarning(Some(I18N.gui.get("tabs.code.cannotCompile")))
    }
  }

  override def dirty_=(d: Boolean) = {
    checkCompilable(d)

    _dirty = d

    if (d) {
      saveNeeded = true

      new WindowEvents.DirtyEvent(Some(filename.merge)).raise(this)
    }
  }

  def filenameForDisplay = (filename map TemporaryCodeTab.stripPath).merge

  def save(saveAs: Boolean) = {
    if (saveAs || filename.isLeft)
      filename = Right(userChooseSavePath())
    FileIO.writeFile(filename.getOrElse(null), text.getText)
    saveNeeded = false
    compileIfDirty()
    dirty = false

    new WindowEvents.ExternalFileSavedEvent(filename.merge).raise(this)
  }

  override def close(): Unit = {
    if (saveNeeded) {
      if (Dialogs.userWantsToSaveFirst(filenameForDisplay, this)) {
        // The user is saving the file with its current name
        save(false)
      }
    }
    closing = true
    tabs.closeExternalFile(_filename)
  }

  def compileIfDirty(): Unit = {
    if (dirty)
      compile()
  }

  override def handle(e: WindowEvents.CompiledEvent) = {
    def setErrorLabel() = errorLabel.setError(Option(e.error), e.sourceOwner.headerSource.size)

    dirty = false
    e.sourceOwner match {
      case file: ExternalFileInterface if file.getFileName == filename.getOrElse(null) => setErrorLabel()
      // if the Code tab compiles then get rid of the error ev 7/26/07
      case tab: CodeTab if e.error == null                                       => setErrorLabel()
      case _ =>
    }
  }

  override def handle(e: AppEvents.SwitchedTabsEvent) = if (!closing) super.handle(e)

  private def userChooseSavePath(): String = {
    def appendIfNecessary(str: String, suffix: String) = if (str.endsWith(suffix)) str else str + suffix

    val newFileName = appendIfNecessary(filenameForDisplay, ".nls")
    val path = SwingFileDialog.showFiles(this, I18N.gui.get("file.save.external"), FileDialog.SAVE, newFileName)
    appendIfNecessary(path, ".nls")
  }
}
