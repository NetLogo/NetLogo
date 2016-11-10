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
import org.nlogo.window.{ Events => WindowEvents, ExternalFileInterface }
import org.nlogo.workspace.AbstractWorkspace

object TemporaryCodeTab {
  private var _serialNum = 1
  def serialNum = {
    _serialNum += 1
    _serialNum - 1
  }
  private[app] def stripPath(filename: String): String =
    filename.substring(filename.lastIndexOf(System.getProperty("file.separator")) + 1, filename.length)
}

class TemporaryCodeTab(workspace: AbstractWorkspace, tabs: TabsInterface, var filename: TabsInterface.Filename, smartIndent: Boolean)
extends CodeTab(workspace, tabs)
with AppEvents.IndenterChangedEvent.Handler {
  var closing = false

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

  override def getAdditionalToolBarComponents: Seq[Component] = Seq(new ToolBarActionButton(FileCloseAction))

  override def dirty_=(b: Boolean) = {
    super.dirty_=(b)
    if (b) new WindowEvents.DirtyEvent(Some(filename.merge)).raise(this)
  }

  override def compile() = {
    save(false)
    super.compile()
  }

  def filenameForDisplay = (filename.right map TemporaryCodeTab.stripPath).merge

  def save(saveAs: Boolean) = {
    if (saveAs || filename.isLeft)
      filename = Right(userChooseSavePath())
    FileIO.writeFile(filename.right.get, text.getText)
    dirty = false
    new WindowEvents.ExternalFileSavedEvent(filename.merge).raise(this)
  }

  def close() {
    ignoring(classOf[UserCancelException]) {
      if(dirty && userWantsToSaveFirst())
        save(false)
      closing = true
      tabs.closeExternalFile(filename)
    }
  }

  def doSave() {
    if(dirty)
      ignoring(classOf[UserCancelException]) {
        save(false)
      }
  }

  override def handle(e: WindowEvents.CompiledEvent) {
    dirty = false
    if((e.sourceOwner.isInstanceOf[ExternalFileInterface] &&
      e.sourceOwner.asInstanceOf[ExternalFileInterface].getFileName == filename.right.get)
      // if the Code tab compiles then get rid of the error ev 7/26/07
      || (e.sourceOwner.isInstanceOf[CodeTab] && e.error == null))
      errorLabel.setError(e.error, e.sourceOwner.headerSource.size)
  }

  override def handle(e: AppEvents.SwitchedTabsEvent) = if (!closing) super.handle(e)

  final def handle(e: AppEvents.IndenterChangedEvent) {
    setIndenter(e.isSmart)
  }

  private def userChooseSavePath(): String = {
    val newFileName = filenameForDisplay + ".nls"
    val path = SwingFileDialog.show(this, I18N.gui.get("file.save.external"), FileDialog.SAVE, newFileName)
    if (path.endsWith(".nls")) path else path + ".nls"
  }

  @throws(classOf[UserCancelException])
  private def userWantsToSaveFirst() = {
    val options = {
      implicit val i18nPrefix = I18N.Prefix("common.buttons")
      Array[AnyRef](I18N.gui("save"), I18N.gui("discard"), I18N.gui("cancel"))
    }
    val message = I18N.gui.getN("file.save.offer.confirm", filenameForDisplay)
    OptionDialog.showMessage(this, I18N.gui.get("common.messages.warning"), message, options) match {
      case 0 => true
      case 1 => false
      case _ => throw new UserCancelException
    }
  }

  private object FileCloseAction extends AbstractAction(I18N.gui.get("tabs.external.close")) {
    override def actionPerformed(e: ActionEvent) = close()
  }
}
