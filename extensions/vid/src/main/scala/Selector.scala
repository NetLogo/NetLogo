package org.nlogo.extensions.vid

import java.awt.Frame
import javax.swing.JFileChooser

import org.nlogo.api.{ AppInterface, ReporterRunnable, Workspace }
import org.nlogo.app.App
import org.nlogo.awt.UserCancelException
import org.nlogo.core.I18N
import org.nlogo.swing.{ DropdownOptionPane, FileDialog }

trait Selector {
  def selectOneOf(choices: Seq[String]): Option[String]
  def selectFile: Option[String]
}

class NetLogoSelector extends Selector {
  private var app: Option[AppInterface] = None

  def setApp(app: Option[AppInterface]): Unit = {
    this.app = app
  }

  private def frame: Option[Frame] = app.map(_.frame)
  private def workspace: Option[Workspace] = app.map(_.workspace)

  override def selectOneOf(choices: Seq[String]): Option[String] = {
    if (VidExtension.isHeadless) {
      None
    } else {
      workspace.flatMap(_.waitForResult(
        new ReporterRunnable[Option[String]] {
          override def run(): Option[String] = {
            frame.flatMap { f =>
              new DropdownOptionPane(f, "Select a Camera", "Choose a camera from the list", choices).getSelectedChoice
            }
          }
        }
      ))
    }
  }

  override def selectFile: Option[String] = {
    if (VidExtension.isHeadless) {
      None
    } else {
      val selectedPath = workspace.flatMap(_.waitForResult(
        new ReporterRunnable[Option[String]] {
          override def run(): Option[String] =
            try {
              frame.map(FileDialog.showFiles(_, "Select a movie to open", JFileChooser.FILES_ONLY))
            } catch {
              case e: UserCancelException => None
              case e: Exception =>
                println(e)
                e.printStackTrace()
                None
            }
        }
      ))

      selectedPath
    }
  }
}
