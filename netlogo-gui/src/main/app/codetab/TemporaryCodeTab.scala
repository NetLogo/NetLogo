// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.{Component, FileDialog}
import java.awt.event.ActionEvent
import java.io.{File, IOException}
import javax.swing.AbstractAction

import scala.util.control.Exception.ignoring
import org.nlogo.api.FileIO
import org.nlogo.app.common.{ Events => AppEvents, TabsInterface }
import org.nlogo.awt.UserCancelException
import org.nlogo.core.I18N
import org.nlogo.swing.{ FileDialog => SwingFileDialog, OptionDialog, ToolBarActionButton }
import org.nlogo.window.{ExternalFileInterface, Events => WindowEvents}
import org.nlogo.workspace.AbstractWorkspace

class TemporaryCodeTab(workspace: AbstractWorkspace, tabs: TabsInterface, var filename: TabsInterface.Filename, smartIndent: Boolean)
extends CodeTab(workspace, tabs)
with AppEvents.IndenterChangedEvent.Handler
{
  filename.right foreach { path =>
        try {
          innerSource = FileIO.file2String(path).replaceAll("\r\n", "\n")
          dirty = false
        } catch {
          case _: IOException => innerSource = ""
        }
     }
  setIndenter(smartIndent)
  lineNumbersVisible = tabs.lineNumbersVisible

  override def getAdditionalToolBarComponents: Seq[Component] = Seq(new ToolBarActionButton(new FileCloseAction))

  override def compile() = {
    save(false)
    super.compile()
  }

  def save(saveAs: Boolean) = {
    if (saveAs || filename.isLeft)
      filename = Right(userChooseSavePath())
    FileIO.writeFile(filename.right.get, text.getText)
    dirty = false
  }

  override def handle(e: WindowEvents.CompiledEvent) {
    dirty = false
    if((e.sourceOwner.isInstanceOf[ExternalFileInterface] &&
        e.sourceOwner.asInstanceOf[ExternalFileInterface].getFileName == filename.right.get)
        // if the Code tab compiles then get rid of the error ev 7/26/07
        || (e.sourceOwner.isInstanceOf[CodeTab] && e.error == null))
      errorLabel.setError(e.error, e.sourceOwner.headerSource.size)
  }

  final def handle(e: AppEvents.IndenterChangedEvent) {
    setIndenter(e.isSmart)
  }

  def close() {
    ignoring(classOf[UserCancelException]) {
      if(dirty && userWantsToSaveFirst())
        save(false)
      tabs.closeExternalFile(filename)
    }
  }

  def doSave() {
    if(dirty)
      ignoring(classOf[UserCancelException]) {
        save(false)
      }
  }

  private def userChooseSavePath(): String = {
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
      OptionDialog.showMessage(
        this, I18N.gui.get("common.messages.error"), "You must choose a name ending with: .nls",
        Array("Try Again"))
    }
    throw new IllegalStateException
  }

  @throws(classOf[UserCancelException])
  private def userWantsToSaveFirst() = {
    val options = Array[AnyRef](I18N.gui.get("common.buttons.save"), "Discard", I18N.gui.get("common.buttons.cancel"))
    val message = "Do you want to save the changes you made to " + filename + "?"
    OptionDialog.showMessage(this, I18N.gui.get("common.messages.warning"), message, options) match {
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
