package org.nlogo.extensions.vid

import javax.swing.JFileChooser

import org.nlogo.api.ReporterRunnable
import org.nlogo.app.App
import org.nlogo.awt.UserCancelException
import org.nlogo.core.I18N
import org.nlogo.swing.{ DropdownOptionPane, FileDialog }

trait Selector {
  def selectOneOf(choices: Seq[String]): Option[String]
  def selectFile: Option[String]
}

object NetLogoSelector extends Selector {

  private def frame = App.app.frame
  private def workspace = App.app.workspace

  override def selectOneOf(choices: Seq[String]): Option[String] = {
    if (VidExtension.isHeadless) {
      None
    } else {
      workspace.waitForResult(
        new ReporterRunnable[Option[String]] {
          override def run(): Option[String] = {
            new DropdownOptionPane(frame, "Select a Camera", "Choose a camera from the list", choices).getSelectedChoice
          }
        }
      )
    }
  }

  override def selectFile: Option[String] = {
    if (VidExtension.isHeadless) {
      None
    } else {
      val selectedPath = workspace.waitForResult(
        new ReporterRunnable[Option[String]] {
          override def run(): Option[String] =
            try {
              val path =
                FileDialog.showFiles(frame, "Select a movie to open", JFileChooser.FILES_ONLY)
              Some(path)
            } catch {
              case e: UserCancelException => None
              case e: Exception =>
                println(e)
                e.printStackTrace()
                None
            }
        }
      )

      selectedPath
    }
  }
}
