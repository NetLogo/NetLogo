// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing;

public final strictfp class FileDialog {

  // this class is not instantiable
  private FileDialog() {
    throw new IllegalStateException();
  }

  /**
   * the current directory. This is the directory the dialog will start in
   * next time it's shown. It's either set explicitly from code, or it's the
   * directory the user last selected a file from.
   */
  private static String currentDirectory = System.getProperty("user.home");

  /**
   * sets the current directory for the file dialog.
   */
  public static void setDirectory(String directory) {
    currentDirectory = directory;
  }

  /**
   * shows the file dialog. The given component's frame will be used, and
   * the given file will be the default selection.
   */
  public static String show(java.awt.Component component, String title,
                            int mode, String file)
      throws org.nlogo.awt.UserCancelException {
    return show(org.nlogo.awt.Hierarchy.getFrame(component),
        title, mode, false, file);
  }

  /**
   * shows the file dialog. The given frame will be used, and there will
   * be no default file selection.
   */
  public static String show(java.awt.Frame parentFrame, String title,
                            int mode)
      throws org.nlogo.awt.UserCancelException {
    return FileDialog.show(parentFrame, title, mode, false);
  }

  public static String show(java.awt.Frame parentFrame, String title,
                            int mode, boolean directoriesOnly)
      throws org.nlogo.awt.UserCancelException {
    return FileDialog.show(parentFrame, title, mode, directoriesOnly, null);
  }

  private static final boolean MAC =
      System.getProperty("os.name").startsWith("Mac");
  private static final boolean LINUX =
      System.getProperty("os.name").startsWith("Linux");
  private static String show(java.awt.Frame parentFrame, String title,
                             int mode, boolean directoriesOnly, String file)
      throws org.nlogo.awt.UserCancelException {
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
    if (LINUX) {
      javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {
        String ext = org.nlogo.api.Version.is3D() ? ".nlogo3d" : ".nlogo";

        @Override
        public boolean accept(java.io.File f) {
          if (f.isDirectory()) {
            return true;
          }
          return f.getName().endsWith(ext);
        }

        @Override
        public String getDescription() {
          return "NetLogo files (*" + ext + ")";
        }
      };
      javax.swing.JFileChooser chooser =
          new javax.swing.JFileChooser(currentDirectory);
      chooser.addChoosableFileFilter(filter);
      chooser.setDialogTitle(title);
      if (file != null && file.length() > 0) {
        chooser.setSelectedFile(new java.io.File(file));
      }
      if (directoriesOnly) {
        chooser.setFileSelectionMode
            (javax.swing.JFileChooser.DIRECTORIES_ONLY);
      }
      int result;
      if (mode == java.awt.FileDialog.LOAD) {
        result = chooser.showOpenDialog(parentFrame);
      } else {
        result = chooser.showSaveDialog(parentFrame);
      }
      if (result != javax.swing.JFileChooser.APPROVE_OPTION) {
        throw new org.nlogo.awt.UserCancelException();
      }
      if(mode == java.awt.FileDialog.LOAD && !new java.io.File(chooser.getSelectedFile().getAbsolutePath()).exists()){
        return show(parentFrame, title, mode, directoriesOnly, chooser.getSelectedFile().getAbsolutePath());
      }
      currentDirectory = selectedDirectory(chooser);
      return chooser.getSelectedFile().getAbsolutePath();
    }
    if (directoriesOnly) {
      javax.swing.JFileChooser chooser =
          new javax.swing.JFileChooser(currentDirectory);
      chooser.setFileSelectionMode
          (javax.swing.JFileChooser.DIRECTORIES_ONLY);
      if (chooser.showOpenDialog(parentFrame)
          != javax.swing.JFileChooser.APPROVE_OPTION) {
        throw new org.nlogo.awt.UserCancelException();
      }
      if(mode == java.awt.FileDialog.LOAD && !new java.io.File(chooser.getSelectedFile().getAbsolutePath()).exists()){
        currentDirectory = chooser.getSelectedFile().getAbsolutePath();
        return show(parentFrame, title, mode, directoriesOnly, currentDirectory);
      }
      currentDirectory = selectedDirectory(chooser);
      return chooser.getSelectedFile().getAbsolutePath();
    }
    java.awt.FileDialog dialog = new java.awt.FileDialog(parentFrame, title, mode);
    if (!directoriesOnly) {
      dialog.setDirectory(currentDirectory); // ???
      if (file != null) {
        dialog.setFile(file);
      }
    }
    dialog.setVisible(true);
    if ((!directoriesOnly && dialog.getFile() == null) ||
        (directoriesOnly && dialog.getDirectory() == null)) {
      throw new org.nlogo.awt.UserCancelException();
    }
    currentDirectory = dialog.getDirectory();
    if(mode == java.awt.FileDialog.LOAD && !new java.io.File(currentDirectory + dialog.getFile()).exists()){
      return show(parentFrame, title, mode, directoriesOnly, dialog.getFile());
    }  
    if (directoriesOnly) {
      return currentDirectory;
    }
    if (dialog.getDirectory() == null) {
      return dialog.getFile();
    } else {
      return dialog.getDirectory() + dialog.getFile();
    }
  }

  private static String selectedDirectory(javax.swing.JFileChooser chooser) {
    java.io.File file = chooser.getSelectedFile();
    java.io.File dir = file.isDirectory() ? file : chooser.getCurrentDirectory();
    return dir.getAbsolutePath();
  }
}
