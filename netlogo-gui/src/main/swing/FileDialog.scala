// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, EventQueue, FileDialog => AWTFileDialog, Frame, Toolkit }
import java.io.File
import javax.swing.JFileChooser

import org.nlogo.api.ModelReader
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
  def showFiles(component: Component, title: String, mode: Int, file: String, allowed: Option[Seq[(String, String)]] = None): String =
    showFiles(Hierarchy.getFrame(component), title, mode, file, allowed)

  def confirmFileOverwrite(owner: Component, path: String) = {
    // The FileDialog checks for overwrite, but we munge extensions after
    // so we need to check with the user to see if they really meant to use
    // the extension so we don't overwrite anything we're not meant to.
    // -Jeremy B June 2021
    if (new OptionPane(owner, I18N.gui.get("common.netlogo"), I18N.gui.getN("file.save.warn.overwrite", path),
                       Seq(I18N.gui.get("common.buttons.replace"), I18N.gui.get("common.buttons.cancel")),
                       OptionPane.Icons.Question).getSelectedIndex != 0) {
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
    showFiles(parentFrame, title, mode, null, None)

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
  private def showFiles(parentFrame: Frame, title: String, mode: Int, file: String, allowed: Option[Seq[(String, String)]]): String = {
    // native dialogs are not recongized by the EDT, so without this SecondaryLoop, the File menu
    // popup remains open in front of the file chooser dialog (Isaac B 7/10/25)
    val loop = Toolkit.getDefaultToolkit.getSystemEventQueue.createSecondaryLoop()

    var selected: String = null

    EventQueue.invokeLater(() => {
      val chooser = NativeFileChooser.createFileChooser

      if (file == null) {
        chooser.setCurrentDirectory(new File(currentDirectory))
      } else {
        chooser.setSelectedFile(new File(file))
      }

      chooser.setDialogTitle(title)
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY)

      allowed.foreach(chooser.setFileTypes)

      val result = {
        if (mode == AWTFileDialog.LOAD) {
          chooser.showOpenDialog(parentFrame)
        } else {
          chooser.setDefaultExtension(ModelReader.modelSuffix)
          chooser.showSaveDialog(parentFrame)
        }
      }

      val selectedFile = chooser.getSelectedFile

      chooser.cleanup()

      if (result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
        currentDirectory = selectedFile.getParentFile.getAbsolutePath

        if (mode == AWTFileDialog.LOAD && !selectedFile.exists) {
          selected = showFiles(parentFrame, title, mode, selectedFile.getAbsolutePath, allowed)
        } else {
          selected = selectedFile.getAbsolutePath
        }
      }

      loop.exit()
    })

    loop.enter()

    if (selected == null)
      throw new UserCancelException

    selected
  }

  private def selectedDirectory(chooser: JFileChooser): String = {
    val file = chooser.getSelectedFile
    val dir = if (file.isDirectory) file else chooser.getCurrentDirectory
    dir.getAbsolutePath
  }
}
