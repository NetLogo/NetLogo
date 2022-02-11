// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Frame }
import java.io.File
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
  def showFiles(component: Component, title: String, mode: Int, file: String): String = {
    println("FileDialog.showFiles(component: Component, title: String, mode: Int, file: String)")
    showFiles(Hierarchy.getFrame(component), title, mode, file)
  }

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
  def showFiles(parentFrame: Frame, title: String, mode: Int): String = {
    println("FileDialog.showFiles(parentFrame: Frame, title: String, mode: Int)")
    showFiles(parentFrame, title, mode, null)
  }

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

  private class LoggingFileDialog(w: Frame, t: String, m: Int) extends java.awt.FileDialog(w, t, m) {

    override def show() = {
      println("FileDialog.show()")
      super.show()
      println("FileDialog.show() complete.")
    }

    override def hide() = {
      println("FileDialog.hide()")
      Thread.dumpStack
      super.hide()
      println("FileDialog.hide() complete.")
    }

    override def setVisible(visible: Boolean) = {
      println(s"FileDialog.setVisible(${visible})")
      Thread.dumpStack()
      super.setVisible(visible)
      println("FileDialog.setVisible() complete.")
    }

  }

  @throws[UserCancelException]
  private def showFiles(parentFrame: Frame, title: String, mode: Int, file: String): String = {
    println(s"FileDialog.showFiles(parentFrame: ${parentFrame}, title: ${title}, mode: ${mode}, file: ${file})")
    val dialog = new LoggingFileDialog(parentFrame, title, mode)
    println("java.awt.FileDialog created.")
    dialog.setDirectory(currentDirectory)
    println(s"Directory set to $currentDirectory.")
    if (file != null) {
      dialog.setFile(file)
      println(s"File set to $file.")
    }
    println("Showing file dialog.")
    dialog.setVisible(true)
    println("Getting user file choice from the dialog.")
    val chosenFile = dialog.getFile
    println(s"File choice complete: ${chosenFile}.")
    if (chosenFile == null) {
      println("Dialog did not return a chosenFile, assume user cancel.")
      throw new UserCancelException
    }
    println("Getting directory.")
    currentDirectory = dialog.getDirectory
    if (mode == java.awt.FileDialog.LOAD && !new File(currentDirectory + chosenFile).exists) {
      println("User selected non-existent chosenFile, re-running showFiles().")
      return showFiles(parentFrame, title, mode, chosenFile)
    }
    println(s"FileDialog.showFiles() complete, return chosenFile or currentDirectory (${currentDirectory}) and chosenFile (${chosenFile}).")
    if (currentDirectory == null)
      chosenFile
    else
      currentDirectory + chosenFile
  }

  private def selectedDirectory(chooser: JFileChooser): String = {
    val file = chooser.getSelectedFile
    val dir = if (file.isDirectory) file else chooser.getCurrentDirectory
    dir.getAbsolutePath
  }
}
