// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm;

import org.nlogo.agent.World;
import org.nlogo.api.CompilerException;
import org.nlogo.api.File;
import org.nlogo.api.FileMode;

public interface FileManager {
  String getPrefix();

  // Prefix is
  String attachPrefix(String filename)
      throws java.net.MalformedURLException;

  void setPrefix(String newPrefix);

  void setPrefix(java.net.URL newPrefix);

  boolean eof()
      throws java.io.IOException;

  File currentFile();

  File findOpenFile(String fileName);

  boolean hasCurrentFile();

  void closeCurrentFile()
      throws java.io.IOException;

  void flushCurrentFile()
      throws java.io.IOException;

  void deleteFile(String filename)
      throws java.io.IOException;

  void closeAllFiles()
      throws java.io.IOException;

  boolean fileExists(String filePath)
      throws java.io.IOException;

  void openFile(String newFileName)
      throws java.io.IOException;

  File getFile(String newFileName);

  void ensureMode(FileMode openMode)
      throws java.io.IOException;

  String getErrorInfo()
      throws java.io.IOException;

  Object read(World world)
      throws java.io.IOException, CompilerException;

  String readLine()
      throws java.io.IOException;

  String readChars(int num)
      throws java.io.IOException;
}
