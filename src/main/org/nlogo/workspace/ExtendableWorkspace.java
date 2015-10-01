
package org.nlogo.workspace;

// this is temporary to support a refactor of ExtensionManager

interface ExtendableWorkspace {
  public boolean compilerTestingMode();
  public String getSource(String filename) throws java.io.IOException;
  public boolean profilingEnabled();
  public org.nlogo.nvm.FileManager fileManager();
  public String attachModelDir(String filePath) throws java.net.MalformedURLException;
  public boolean warningMessage(String message);
  public Object readFromString(String path) throws org.nlogo.api.CompilerException;
}
