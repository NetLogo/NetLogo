// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.awt.{ Component, FileDialog => AWTFileDialog }
import java.awt.event.ActionEvent
import java.io.IOException
import javax.swing.{ AbstractAction => SwingAbstractAction, JDialog, JOptionPane }

import org.nlogo.core.I18N
import org.nlogo.api.Exceptions
import org.nlogo.awt.{ EventQueue, Hierarchy => NLogoHierarchy, UserCancelException }
import org.nlogo.swing.{ FileDialog, Implicits, ModalProgressTask }, Implicits.thunk2runnable
import org.nlogo.window.{ ExportControls, GUIWorkspace, SwingUnlockedExecutionContext }
import org.nlogo.workspace.{ OpenModel, SaveModel, SaveModelAs }

import scala.concurrent.Future

object Actions {
  val Ellipsis = '\u2026'
}

import Actions._

abstract class ExportBackgroundAction[A](parent: Component, taskName: String, suggestedFileName: String)
extends SwingAbstractAction(I18N.gui.get("menu.file.export." + taskName) + Ellipsis) {

  def beforeModalDialog(): A

  def inModalDialog(a: A, closeDialog: () => Unit): Unit

  def runDialog(a: A)(dialog: JDialog): Unit = {
    // we use isDisposed to allow this callback to be run multiple times safely
    var isDisposed = false
    val closeDialog = { () =>
      EventQueue.invokeLater { () =>
        if (! isDisposed) {
          dialog.setVisible(false)
          dialog.dispose()
          isDisposed = true
        }
      }
    }
    inModalDialog(a, closeDialog)
  }

  def actionPerformed(e: ActionEvent): Unit = {
    val aOpt =
      try {
        Some(beforeModalDialog())
      } catch {
        case ex: UserCancelException =>
          Exceptions.ignore(ex)
          None
      }
    aOpt.foreach(a =>
      ModalProgressTask.display(NLogoHierarchy.getFrame(parent),
        I18N.gui.get("dialog.interface.export.task"),
        runDialog(a)))
  }

  def promptForFilePath(): String = {
    FileDialog.show(NLogoHierarchy.getFrame(parent),
      I18N.gui.get(s"menu.file.export.$taskName"),
      AWTFileDialog.SAVE, suggestedFileName)
  }

  def handleIOException(ex: Exception): Unit = {
    ExportControls.displayExportError(parent, ex.getMessage)
  }
}

class ExportInterfaceAction(parent: Component, workspace: GUIWorkspace)
    extends ExportBackgroundAction[String](parent, "interface", workspace.guessExportName("interface.png")) {
    def beforeModalDialog(): String = promptForFilePath()

    def inModalDialog(filename: String, closeDialog: () => Unit): Unit = {
      workspace.exportInterfaceFromUIThread(filename, closeDialog)
    }
  }

