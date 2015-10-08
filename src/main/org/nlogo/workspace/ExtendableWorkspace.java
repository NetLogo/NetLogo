// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace;

/* This is the workspace interface visible to extensions.
 * extensions can always cast the workspace, but this documents
 * what parts of the workspace are visible to an extension.
 */

public interface ExtendableWorkspace {
  public void setProfilingTracer(org.nlogo.nvm.Tracer tracer);
  public boolean compilerTestingMode();
  public String getSource(String filename) throws java.io.IOException;
  public boolean profilingEnabled();
  public org.nlogo.nvm.FileManager fileManager();
  public String attachModelDir(String filePath) throws java.net.MalformedURLException;
  public boolean warningMessage(String message);
  public Object readFromString(String path) throws org.nlogo.api.CompilerException;
}
