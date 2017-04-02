// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Frame }
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter

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
  def show(component: Component, title: String, mode: Int, file: String): String =
    show(Hierarchy.getFrame(component), title, mode, false, file)

  /**
    * shows the file dialog. The given frame will be used, and there will
    * be no default file selection.
    */
  @throws[UserCancelException]
  def show(parentFrame: Frame, title: String, mode: Int): String =
    show(parentFrame, title, mode, false)

  @throws[UserCancelException]
  def show(parentFrame: Frame, title: String, mode: Int, directoriesOnly: Boolean): String =
    show(parentFrame, title, mode, directoriesOnly, null)

  @throws[UserCancelException]
  private def show(parentFrame: Frame, title: String, mode: Int, directoriesOnly: Boolean, file: String): String = {
    import java.awt.FileDialog.{ LOAD, SAVE }

    val isLinux = System.getProperty("os.name").startsWith("Linux")
    val shouldUseJFileChooser = isLinux || directoriesOnly

    // On Linux:
    // 1) java.awt.FileDialog has a weird bug where it returns OK even
    //       if the user clicks the X on the dialog to cancel it.
    // 2) The AWT dialog looks unbelievably ugly, shows all the hidden files
    //  and folders in my HOME directory (and yes, there are a million),
    //  won't autocomplete names, won't let me start typing names to select files,
    //    and generally hates me as much as I hate it.
    // I suspect my Linux-specific code could replace all the Mac/Windows code below,
    // but that code already seems strangely more complicated than expected, so I decided
    // not to mess with it.  Probably it's just left-over cruft from a pre-JFileChooser era,
    // but I'm not sure.
    //  ~Forrest (6/8/2009)
    if (shouldUseJFileChooser) {
      val netlogoFilter = new FileFilter {
        override val getDescription = "NetLogo Files (*.nlogo, *.nlogo3d)"

        override def accept(f: File) =
          if (f.isDirectory)
            true
          else
            f.getName.endsWith(".nlogo") || f.getName.endsWith(".nlogo3d")
      }
      val chooser = new JFileChooser(currentDirectory)
      if (!directoriesOnly)
        chooser.addChoosableFileFilter(netlogoFilter)
      chooser.setDialogTitle(title)
      if (file != null && file.length > 0) {
        chooser.setSelectedFile(new File(file))
      }
      if (directoriesOnly)
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
      val result: Int =
        if (mode == LOAD)
          chooser.showOpenDialog(parentFrame)
        else
          chooser.showSaveDialog(parentFrame)
      if (result != JFileChooser.APPROVE_OPTION)
        throw new UserCancelException
      if (mode == LOAD && !new File(chooser.getSelectedFile.getAbsolutePath).exists) {
        if (directoriesOnly)
          currentDirectory = chooser.getSelectedFile.getAbsolutePath
        return show(parentFrame, title, mode, directoriesOnly, chooser.getSelectedFile.getAbsolutePath)
      }
      currentDirectory = selectedDirectory(chooser)
      chooser.getSelectedFile.getAbsolutePath
    } else {
      val dialog = new java.awt.FileDialog(parentFrame, title, mode)
      dialog.setDirectory(currentDirectory) // ???
      if (file != null)
        dialog.setFile(file)
      dialog.setVisible(true)
      if (dialog.getFile == null)
        throw new UserCancelException
      currentDirectory = dialog.getDirectory
      if (mode == LOAD && !new File(currentDirectory + dialog.getFile).exists)
        return show(parentFrame, title, mode, false, dialog.getFile)
      if (dialog.getDirectory == null)
        dialog.getFile
      else
        dialog.getDirectory + dialog.getFile
    }
  }

  private def selectedDirectory(chooser: JFileChooser): String = {
    val file = chooser.getSelectedFile
    val dir = if (file.isDirectory) file else chooser.getCurrentDirectory
    dir.getAbsolutePath
  }
}
