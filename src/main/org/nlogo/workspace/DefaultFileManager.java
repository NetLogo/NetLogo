// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace;

import org.nlogo.api.I18N;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final strictfp class DefaultFileManager
    implements org.nlogo.nvm.FileManager {

  private final List<org.nlogo.api.File> openFiles =
      new ArrayList<org.nlogo.api.File>();
  private org.nlogo.api.File currentFile;
  private String prefix;
  private final AbstractWorkspace workspace;

  public DefaultFileManager(AbstractWorkspace workspace) {
    this.workspace = workspace;
  }

  public String getErrorInfo()
      throws java.io.IOException {
    long position = currentFile.pos();

    currentFile.close(true);
    currentFile.open(org.nlogo.api.FileModeJ.READ());

    int lineNumber = 1;
    long prevPosition = 0;
    String lastLine = readLine();

    while (currentFile.pos() < position) {
      lineNumber++;
      prevPosition = currentFile.pos();
      lastLine = readLine();
    }

    int charPos = (int) (position - prevPosition);

    // This will happen if we are at the end of a line
    if (charPos >= lastLine.length() && !eof()) {
      lastLine = readLine();
      charPos = 0;
      lineNumber++;
    }

    closeCurrentFile();

    return
        " (line number " + lineNumber +
            ", character " + (charPos + 1) + ")";
  }

  public String getPrefix() {
    return prefix;
  }

  public org.nlogo.api.File getFile(String filename) {
    return new org.nlogo.api.LocalFile(filename);
  }

  public void setPrefix(String newPrefix) {
    // Ensure a slash so it isAbsolute() won't get mixed up with getModelDir()
    if (newPrefix.charAt(newPrefix.length() - 1) != java.io.File.separatorChar) {
      newPrefix += java.io.File.separatorChar;
    }

    if (new java.io.File(newPrefix).isAbsolute()) {
      prefix = newPrefix;
    } else {
      prefix = relativeToAbsolute(newPrefix);
      if (prefix.charAt(prefix.length() - 1) != java.io.File.separatorChar) {
        prefix += java.io.File.separatorChar;
      }
    }
  }

  public void setPrefix(java.net.URL newPrefix) {
    prefix = newPrefix.toString();
  }

  public String attachPrefix(String filename)
      throws java.net.MalformedURLException {
    // Check to see if we were given an absolute File Path
    java.io.File fileForm = new java.io.File(filename);

    if (fileForm.isAbsolute() || prefix == null) {
      return filename;
    } else {
      return relativeToAbsolute(filename);
    }
  }

  // Automatically parses relative file names
  private String relativeToAbsolute(String newPath) {
    try {
      return new java.io.File(prefix + java.io.File.separatorChar + newPath)
          .getCanonicalPath();
    } catch (java.io.IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public org.nlogo.api.File currentFile() {
    return hasCurrentFile() ? currentFile : null;
  }

  public boolean hasCurrentFile() {
    return (currentFile != null && isFileOpen(currentFile.getAbsolutePath()));
  }

  private boolean isFileOpen(String filename) {
    return (findOpenFile(filename) != null);
  }

  public org.nlogo.api.File findOpenFile(String filename) {
    java.io.File newFile = new java.io.File(filename);

    Iterator<org.nlogo.api.File> files = openFiles.iterator();
    while (files.hasNext()) {
      org.nlogo.api.File nextFile = files.next();
      if (newFile.getAbsolutePath().equals(nextFile.getAbsolutePath())) {
        return nextFile;
      }
    }
    return null;
  }

  private boolean isFileCurrent(org.nlogo.api.File checkFile) {
    return (currentFile == checkFile);
  }

  public void setCurrentFile(org.nlogo.api.File newFile) {
    if (!isFileCurrent(newFile)) {
      currentFile = newFile;
    }
  }

  public void ensureMode(org.nlogo.api.FileMode openMode)
      throws java.io.IOException {
    if (!hasCurrentFile()) {
      throw new java.io.IOException(I18N.errorsJ().get("org.nlogo.workspace.DefaultFileManager.noOpenFile"));
    }

    if (currentFile.mode() == org.nlogo.api.FileModeJ.NONE()) {
      try {
        currentFile.open(openMode);
      } catch (java.io.FileNotFoundException ex) {
        throw new java.io.IOException("The file " + currentFile.getAbsolutePath() + " cannot be found");
      } catch (java.io.IOException ex) {
        throw new java.io.IOException(ex.getMessage());
      }
    } else if (currentFile.mode() != openMode) {
      String mode = (currentFile.mode() == org.nlogo.api.FileModeJ.READ()) ? "READING" : "WRITING";

      throw new java.io.IOException("You can only use " + mode + " primitives with this file");
    }
  }

  public boolean fileExists(String filePath) {
    return new java.io.File(filePath).exists();
  }

  public void deleteFile(String filePath)
      throws java.io.IOException {
    org.nlogo.api.File file = findOpenFile(filePath);

    if (file != null) {
      throw new java.io.IOException("You need to close the file before deletion");
    }
    java.io.File checkFile = new java.io.File(filePath);
    if (!checkFile.exists()) {
      throw new java.io.IOException(I18N.errorsJ().get("org.nlogo.workspace.DefaultFileManager.cannotDeleteNonExistantFile"));
    }
    if (!checkFile.canWrite()) {
      throw new java.io.IOException("Modification to this file is denied.");
    }
    if (!checkFile.isFile()) {
      throw new java.io.IOException(I18N.errorsJ().get("org.nlogo.workspace.DefaultFileManager.canOnlyDeleteFiles"));
    }

    if (!checkFile.delete()) {
      throw new java.io.IOException("Deletion failed.");
    }
  }

  public void openFile(String newFileName)
      throws java.io.IOException {

    // Check to see if we already opened the file
    String fullFileName = attachPrefix(newFileName);

    if (fullFileName == null) {
      throw new java.io.IOException("This filename is illegal, " + newFileName);
    }

    org.nlogo.api.File newFile = findOpenFile(fullFileName);

    if (newFile == null) {
      newFile = new org.nlogo.api.LocalFile(fullFileName);
      openFiles.add(newFile);
    }

    setCurrentFile(newFile);
  }

  public void flushCurrentFile()
      throws java.io.IOException {
    if (!hasCurrentFile()) {
      throw new java.io.IOException("There is no file to file");
    }
    flushFile(currentFile.getAbsolutePath());
  }

  public void flushFile(String flushFileName) {
    org.nlogo.api.File flushFile = findOpenFile(flushFileName);
    flushFile.flush();
  }

  public void closeCurrentFile()
      throws java.io.IOException {
    if (!hasCurrentFile()) {
      closeAllFiles();
      throw new java.io.IOException("There is no file to close");
    }
    closeFile(currentFile.getAbsolutePath());
    setCurrentFile(null);
  }

  // currently needs absolute file name
  private void closeFile(String closeFileName)
      throws java.io.IOException {
    org.nlogo.api.File closeFile = findOpenFile(closeFileName);
    closeFile.close(true);
    openFiles.remove(closeFile);
  }


  public String readLine()
      throws java.io.IOException {
    if (eof()) {
      throw new java.io.EOFException();
    }

    // Needed to write my own version to keep track of the File's Position
    java.io.BufferedReader buffReader = currentFile.reader();
    String retString = "";
    char charbuff[] = new char[80];
    int charsRead = 80;
    int skip = 1;

    while (charsRead == 80) {
      buffReader.mark(82);
      charsRead = buffReader.read(charbuff, 0, 80);
      for (int i = 0; i < charsRead; i++) {
        // 'i' will never be 80
        if (charbuff[i] == '\r') {
          if ((i < 79 && charbuff[i + 1] == '\n') ||
              (i == 79 && buffReader.read() == '\n')) {
            skip = 2;
          }
          charsRead = i;
        } else if (charbuff[i] == '\n') {
          charsRead = i;
        }
      }
      currentFile.pos_$eq(currentFile.pos() + charsRead);
      retString += new String(charbuff, 0, charsRead);
    }
    buffReader.reset();

    // Doesn't include 'skip' when at the end of file
    currentFile.pos_$eq(currentFile.pos() + buffReader.skip(charsRead + skip) - charsRead);

    return retString;
  }

  public String readChars(int num)
      throws java.io.IOException {
    if (eof()) {
      throw new java.io.EOFException();
    }

    char charbuff[] = new char[num];
    java.io.BufferedReader buffReader = currentFile.reader();
    currentFile.pos_$eq(currentFile.pos() + buffReader.read(charbuff, 0, num));
    return String.valueOf(charbuff);
  }

  public String readRemainder()
      throws java.io.IOException {
    if (eof()) {
      throw new java.io.EOFException();
    }

    // Reads remainder of file
    String remainder = "";
    String line = readLine();

    while (line != null) {
      remainder += line;
      line = readLine();
      currentFile.pos_$eq(currentFile.pos() + line.length());
    }
    return remainder;
  }

  public Object read(org.nlogo.agent.World world)
      throws java.io.IOException {
    if (eof()) {
      throw new java.io.EOFException();
    }
    return workspace.compiler().readFromFile
        (currentFile, world, workspace.getExtensionManager());
  }

  public boolean eof()
      throws java.io.IOException {
    ensureMode(org.nlogo.api.FileModeJ.READ());
    if (!currentFile.eof()) {
      java.io.BufferedReader buffReader = currentFile.reader();
      buffReader.mark(2);
      currentFile.eof_$eq(buffReader.read() == -1);
      buffReader.reset();
    }
    return currentFile.eof();
  }

  public void closeAllFiles()
      throws java.io.IOException {
    Iterator<org.nlogo.api.File> files = openFiles.iterator();
    while (files.hasNext()) {
      org.nlogo.api.File nextFile = files.next();
      closeFile(nextFile.getAbsolutePath());
      files = openFiles.iterator();
    }
    setCurrentFile(null);
  }

  public void writeOutputObject(org.nlogo.agent.OutputObject oo) {
    java.io.PrintWriter w = currentFile.getPrintWriter();
    w.print(oo.get());
  }

  public void handleModelChange() {
    if (workspace.getModelDir() != null) {
      setPrefix(workspace.getModelDir());
    }
    try {
      closeAllFiles();
    } catch (java.io.IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

}
