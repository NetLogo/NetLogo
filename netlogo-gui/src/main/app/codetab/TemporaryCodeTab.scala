// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.FileDialog
import java.awt.event.ActionEvent
import java.io.File
import javax.swing.{ AbstractAction, JButton }

import scala.util.control.Exception.ignoring
import scala.util.Try

import org.nlogo.api.{ FileIO, LocalFile }
import org.nlogo.app.Tabs
import org.nlogo.app.common.{ Events => AppEvents, FindDialog, TabsInterface }
import org.nlogo.awt.UserCancelException
import org.nlogo.core.{ FileMode, I18N }
import org.nlogo.swing.{ FileDialog => SwingFileDialog, OptionDialog, ToolBar }
import org.nlogo.window.{ Events => WindowEvents, ExternalFileInterface }
import org.nlogo.workspace.AbstractWorkspace

object TemporaryCodeTab {
  val NewFile = "New File"
}
class TemporaryCodeTab(workspace: AbstractWorkspace,
                             tabs: TabsInterface,
                             var filename: String,
                             fileMustExist: Boolean,
                             smartIndent: Boolean)
extends CodeTab(workspace)
with AppEvents.IndenterChangedEvent.Handler
with WindowEvents.LoadBeginEvent.Handler
with WindowEvents.AboutToQuitEvent.Handler
{

  val includesMenu = new IncludesMenu(this, tabs)
  load(fileMustExist)
  setIndenter(smartIndent)
  lineNumbersVisible = tabs.lineNumbersVisible

  override def getToolBar =
    new ToolBar {
      override def addControls() {
        add(new JButton(FindDialog.FIND_ACTION))
        add(new JButton(compileAction))
        add(new ToolBar.Separator)
        add(new JButton(new FileCloseAction))
        add(new ToolBar.Separator)
        add(new ProceduresMenu(TemporaryCodeTab.this))
        add(includesMenu)
      }
    }

  private var _dirty = false
  def cleanse() { _dirty = false }
  override def dirty() { _dirty = true }
  def isDirty = _dirty

  private def load(fileMustExist: Boolean) {
    if (filename == "Aggregate")
      throw new Exception("Incorrect error direction!")
    if (filename != TemporaryCodeTab.NewFile) {
      try {
        val sourceFile = new LocalFile(filename)
        if (sourceFile.reader == null)
          sourceFile.open(FileMode.Read)
        val origSource = FileIO.reader2String(sourceFile.reader)
        innerSource = origSource.replaceAll("\r\n", "\n")
        _dirty = false
        _needsCompile = false
        return
      }
      catch {
        case _: java.io.IOException => assert(!fileMustExist)
      }
    }
    innerSource = ""
  }

  override def handle(e: WindowEvents.CompiledEvent) {
    _needsCompile = false
    compileAction.setEnabled(e.error != null)
    if((e.sourceOwner.isInstanceOf[ExternalFileInterface] &&
        e.sourceOwner.asInstanceOf[ExternalFileInterface].getFileName == filename)
        // if the Code tab compiles then get rid of the error ev 7/26/07
        || (e.sourceOwner.isInstanceOf[CodeTab] && e.error == null))
      errorLabel.setError(e.error, e.sourceOwner.headerSource.size)
  }

  def handle(e: WindowEvents.LoadBeginEvent) {
    close()
  }

  def handle(e: WindowEvents.AboutToQuitEvent) {
    close()
  }

  final def handle(e: AppEvents.IndenterChangedEvent) {
    setIndenter(e.isSmart)
  }

  def close() {
    ignoring(classOf[UserCancelException]) {
      if(_dirty && userWantsToSaveFirst)
        save()
      tabs.closeTemporaryFile(filename)
    }
  }

  def doSave() {
    if(_dirty)
      ignoring(classOf[UserCancelException]) {
        save()
      }
  }

  @throws(classOf[UserCancelException])
  def save() {
    if(filename == TemporaryCodeTab.NewFile) {
      filename = userChooseSavePath
      tabs.saveTemporaryFile(filename)
      dirty()
    }
    if(isDirty) {
      // ought to handle IOException here? - ST 8/8/10
      FileIO.writeFile(filename, text.getText())
      cleanse()
    }
  }

  @throws(classOf[UserCancelException])
  private def userChooseSavePath: String = {
    var newFileName = ""
    while(true) {
      val path = SwingFileDialog.show(
        this, "Save NetLogo Source File", FileDialog.SAVE, newFileName)
      val file = new File(path)
      newFileName = file.getName
      val suffixIndex = newFileName.lastIndexOf(".nls")
      // make sure it ends with .nls and there's at least one character first.
      if(suffixIndex > 0 && suffixIndex == newFileName.size - 4)
        return path
      val dotIndex = newFileName.lastIndexOf('.')
      if(dotIndex != -1)
        newFileName = newFileName.substring(0, dotIndex)
      newFileName ++= ".nls"
      OptionDialog.show(
        this, I18N.gui.get("common.messages.error"), "You must choose a name ending with: .nls",
        Array("Try Again"))
    }
    throw new IllegalStateException
  }

  @throws(classOf[UserCancelException])
  private def userWantsToSaveFirst = {
    val options = Array[AnyRef](I18N.gui.get("common.buttons.save"), "Discard", I18N.gui.get("common.buttons.cancel"))
    val message = "Do you want to save the changes you made to " + filename + "?"
    OptionDialog.show(this, I18N.gui.get("common.messages.warning"), message, options) match {
      case 0 => true
      case 1 => false
      case _ => throw new UserCancelException
    }
  }

  private class FileCloseAction extends AbstractAction("Close") {
    override def actionPerformed(e: ActionEvent) {
      close()
    }
  }

}
