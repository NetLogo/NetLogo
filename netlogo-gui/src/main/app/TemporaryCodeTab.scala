// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.core.{ FileMode, I18N }
import scala.util.control.Exception.ignoring
import org.nlogo.awt.UserCancelException
import org.nlogo.workspace.AbstractWorkspace
import org.nlogo.api.{ FileIO, LocalFile}, FileIO.reader2String

object TemporaryCodeTab {
  val NewFile = "New File"
}
class TemporaryCodeTab(workspace: AbstractWorkspace,
                             tabs: Tabs,
                             var filename: String,
                             fileMustExist: Boolean,
                             smartIndent: Boolean)
extends CodeTab(workspace)
with org.nlogo.app.Events.IndenterChangedEvent.Handler
with org.nlogo.window.Events.LoadBeginEvent.Handler
with org.nlogo.window.Events.AboutToQuitEvent.Handler
{

  val includesMenu = new IncludesMenu(this)
  load(fileMustExist)
  setIndenter(smartIndent)
  lineNumbersVisible = tabs.codeTab.lineNumbersVisible

  override def getToolBar =
    new org.nlogo.swing.ToolBar() {
      override def addControls() {
        add(new javax.swing.JButton(org.nlogo.app.FindDialog.FIND_ACTION))
        add(new javax.swing.JButton(compileAction))
        add(new org.nlogo.swing.ToolBar.Separator)
        add(new javax.swing.JButton(new FileCloseAction))
        add(new org.nlogo.swing.ToolBar.Separator)
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
        val origSource = reader2String(sourceFile.reader)
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

  override def handle(e: org.nlogo.window.Events.CompiledEvent) {
    _needsCompile = false
    compileAction.setEnabled(e.error != null)
    if((e.sourceOwner.isInstanceOf[org.nlogo.window.ExternalFileInterface] &&
        e.sourceOwner.asInstanceOf[org.nlogo.window.ExternalFileInterface].getFileName == filename)
        // if the Code tab compiles then get rid of the error ev 7/26/07
        || (e.sourceOwner.isInstanceOf[CodeTab] && e.error == null))
      errorLabel.setError(e.error, e.sourceOwner.headerSource.size)
  }

  def handle(e: org.nlogo.window.Events.LoadBeginEvent) {
    close()
  }

  def handle(e: org.nlogo.window.Events.AboutToQuitEvent) {
    close()
  }

  final def handle(e: org.nlogo.app.Events.IndenterChangedEvent) {
    setIndenter(e.isSmart)
  }

  def close() {
    ignoring(classOf[UserCancelException]) {
      if(_dirty && userWantsToSaveFirst)
        save()
      tabs.closeTemporaryFile(this)
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
      tabs.saveTemporaryFile(this, filename)
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
      val path = org.nlogo.swing.FileDialog.show(
        this, "Save NetLogo Source File", java.awt.FileDialog.SAVE, newFileName)
      val file = new java.io.File(path)
      newFileName = file.getName
      val suffixIndex = newFileName.lastIndexOf(".nls")
      // make sure it ends with .nls and there's at least one character first.
      if(suffixIndex > 0 && suffixIndex == newFileName.size - 4)
        return path
      val dotIndex = newFileName.lastIndexOf('.')
      if(dotIndex != -1)
        newFileName = newFileName.substring(0, dotIndex)
      newFileName ++= ".nls"
      org.nlogo.swing.OptionDialog.show(
        this, I18N.gui.get("common.messages.error"), "You must choose a name ending with: .nls",
        Array("Try Again"))
    }
    throw new IllegalStateException
  }

  @throws(classOf[UserCancelException])
  private def userWantsToSaveFirst = {
    val options = Array[AnyRef](I18N.gui.get("common.buttons.save"), "Discard", I18N.gui.get("common.buttons.cancel"))
    val message = "Do you want to save the changes you made to " + filename + "?"
    org.nlogo.swing.OptionDialog.show(this, I18N.gui.get("common.messages.warning"), message, options) match {
      case 0 => true
      case 1 => false
      case _ => throw new UserCancelException
    }
  }

  private class FileCloseAction extends javax.swing.AbstractAction("Close") {
    override def actionPerformed(e: java.awt.event.ActionEvent) {
      close()
    }
  }

}
