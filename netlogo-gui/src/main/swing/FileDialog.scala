// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, FileDialog => AWTFileDialog, Frame }
import java.io.File
import javax.swing.filechooser.FileFilter
import javax.swing.JFileChooser

import org.nlogo.awt.{ Hierarchy, UserCancelException }
import org.nlogo.core.I18N

object FileDialog {
  /**
    * the current directory. This is the directory the dialog will start in
    * next time it's shown. It's either set explicitly from code, or it's the
    * directory the user last selected a file from.
    */
  private var currentDirectory = System.getProperty("user.home")

  /**
    * sets the current directory for the file dialog.
    */
  def setDirectory(directory: String) = currentDirectory = directory

  /**
    * shows the file dialog. The given component's frame will be used, and
    * the given file will be the default selection.
    */
  @throws[UserCancelException]
  def showFiles(component: Component, title: String, mode: Int, file: String, allowed: List[String] = Nil): String =
    showFiles(Hierarchy.getFrame(component), title, mode, file, allowed)

  def confirmFileOverwrite(owner: Component, path: String) = {
    // The FileDialog checks for overwrite, but we munge extensions after
    // so we need to check with the user to see if they really meant to use
    // the extension so we don't overwrite anything we're not meant to.
    // -Jeremy B June 2021
    val options = Array[Object](
      I18N.gui.get("common.buttons.replace"),
      I18N.gui.get("common.buttons.cancel"))
    val message = I18N.gui.getN("file.save.warn.overwrite", path)
    if (OptionDialog.showMessage(owner, "NetLogo", message, options) != 0) {
      None
    } else {
      Some(path)
    }
  }

  /**
    * shows the file dialog. The given frame will be used, and there will
    * be no default file selection.
    */
  @throws[UserCancelException]
  def showFiles(parentFrame: Frame, title: String, mode: Int): String =
    showFiles(parentFrame, title, mode, null, Nil)

  @throws[UserCancelException]
  def showDirectories(parentFrame: Frame, title: String): String = {
    val chooser = new JFileChooser(currentDirectory)
    chooser.setDialogTitle(title)
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
    if (chooser.showOpenDialog(parentFrame) != JFileChooser.APPROVE_OPTION)
      throw new UserCancelException
    currentDirectory = selectedDirectory(chooser)
    if (!chooser.getSelectedFile.exists)
      return showDirectories(parentFrame, title)
    chooser.getSelectedFile.getAbsolutePath
  }

  @throws[UserCancelException]
  private def showFiles(parentFrame: Frame, title: String, mode: Int, file: String, allowed: List[String]): String = {
    val chooser = new JFileChooser(currentDirectory)
    chooser.setDialogTitle(title)
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY)
    if (file != null)
      chooser.setSelectedFile(new File(file))
    if (!allowed.isEmpty) {
      chooser.setAcceptAllFileFilterUsed(false)
      chooser.setFileFilter(new FileFilter {
        def accept(file: File): Boolean =
          file.isDirectory || allowed.exists(x => file.getName.endsWith("." + x))
        def getDescription(): String =
          allowed.map(x => "*." + x).mkString(", ")
      })
    }
    var result = 0
    if (mode == AWTFileDialog.SAVE) {
      chooser.setDialogType(JFileChooser.SAVE_DIALOG)
      result = chooser.showSaveDialog(parentFrame)
    }
    else {
      chooser.setDialogType(JFileChooser.OPEN_DIALOG)
      result = chooser.showOpenDialog(parentFrame)
    }
    if (result != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile == null)
      throw new UserCancelException
    currentDirectory = selectedDirectory(chooser)
    if (chooser.getSelectedFile.exists)
      chooser.getSelectedFile.getCanonicalPath
    else
      showFiles(parentFrame, title, mode, file, allowed)
  }

  private def selectedDirectory(chooser: JFileChooser): String = {
    val file = chooser.getSelectedFile
    val dir = if (file.isDirectory) file else chooser.getCurrentDirectory
    dir.getAbsolutePath
  }
}
