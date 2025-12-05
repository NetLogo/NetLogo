// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, FileDialog => AWTFileDialog, Frame }
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
  private var currentDirectory: Option[String] = None

  /**
    * sets the current directory for the file dialog.
    */
  def setDirectory(directory: String): Unit = {
    currentDirectory = Option(directory)
  }

  def getDirectory: String =
    currentDirectory.getOrElse(System.getProperty("user.home"))

  /**
    * shows the file dialog. The given component's frame will be used, and
    * the given file will be the default selection.
    */
  @throws[UserCancelException]
  def showFiles(component: Component, title: String, mode: Int, file: String, allowed: List[String] = Nil): String =
    showFiles(Hierarchy.getFrame(component), title, mode, new File(file), allowed)

  private def confirmFileOverwrite(owner: Component, path: String): Boolean = {
    new OptionPane(owner, I18N.gui.get("common.netlogo"), I18N.gui.getN("file.save.warn.overwrite", path),
                   Seq(I18N.gui.get("common.buttons.replace"), I18N.gui.get("common.buttons.cancel")),
                   OptionPane.Icons.Question).getSelectedIndex == 0
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
    val chooser = new JFileChooser(getDirectory)
    chooser.setDialogTitle(title)
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
    if (chooser.showOpenDialog(parentFrame) != JFileChooser.APPROVE_OPTION)
      throw new UserCancelException
    setDirectory(selectedDirectory(chooser))
    if (!chooser.getSelectedFile.exists)
      return showDirectories(parentFrame, title)
    chooser.getSelectedFile.getAbsolutePath
  }

  @throws[UserCancelException]
  private def showFiles(parentFrame: Frame, title: String, mode: Int, file: File, allowed: List[String]): String = {
    val chooser = new JFileChooser(getDirectory) {
      override def approveSelection(): Unit = {
        val path = getSelectedFile.getAbsolutePath
        val munged = org.nlogo.api.FileIO.ensureExtension(path, org.nlogo.api.ModelReader.modelSuffix)

        if (!new File(munged).exists || confirmFileOverwrite(this, munged))
          super.approveSelection()
      }
    }
    chooser.setDialogTitle(title)
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY)
    if (file != null)
      chooser.setSelectedFile(file)
    val result = {
      if (mode == AWTFileDialog.LOAD) {
        chooser.showOpenDialog(parentFrame)
      } else {
        chooser.showSaveDialog(parentFrame)
      }
    }
    if (result != JFileChooser.APPROVE_OPTION)
      throw new UserCancelException
    setDirectory(selectedDirectory(chooser))
    if (mode == AWTFileDialog.LOAD && !chooser.getSelectedFile.exists)
      return showFiles(parentFrame, title, mode, chooser.getSelectedFile, allowed)
    chooser.getSelectedFile.getAbsolutePath
  }

  private def selectedDirectory(chooser: JFileChooser): String = {
    val file = chooser.getSelectedFile
    val dir = if (file.isDirectory) file else chooser.getCurrentDirectory
    dir.getAbsolutePath
  }
}
