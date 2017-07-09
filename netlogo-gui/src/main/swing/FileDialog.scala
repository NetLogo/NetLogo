// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Frame }
import java.io.File
import javax.swing.JFileChooser

import org.nlogo.awt.{ Hierarchy, UserCancelException }

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
  def showFiles(component: Component, title: String, mode: Int, file: String): String =
    showFiles(Hierarchy.getFrame(component), title, mode, file)

  /**
    * shows the file dialog. The given frame will be used, and there will
    * be no default file selection.
    */
  @throws[UserCancelException]
  def showFiles(parentFrame: Frame, title: String, mode: Int): String =
    showFiles(parentFrame, title, mode, null)

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
  private def showFiles(parentFrame: Frame, title: String, mode: Int, file: String): String = {
    val dialog = new java.awt.FileDialog(parentFrame, title, mode)
    dialog.setDirectory(currentDirectory)
    if (file != null)
      dialog.setFile(file)
    dialog.setVisible(true)
    if (dialog.getFile == null)
      throw new UserCancelException
    currentDirectory = dialog.getDirectory
    if (mode == java.awt.FileDialog.LOAD && !new File(currentDirectory + dialog.getFile).exists)
      return showFiles(parentFrame, title, mode, dialog.getFile)
    if (dialog.getDirectory == null)
      dialog.getFile
    else
      dialog.getDirectory + dialog.getFile
  }

  private def selectedDirectory(chooser: JFileChooser): String = {
    val file = chooser.getSelectedFile
    val dir = if (file.isDirectory) file else chooser.getCurrentDirectory
    dir.getAbsolutePath
  }
}
