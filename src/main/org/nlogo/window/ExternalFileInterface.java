// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

public strictfp class ExternalFileInterface
    implements org.nlogo.api.SourceOwner {

  private final String fileName;

  public ExternalFileInterface(String fileName) {
    this.fileName = fileName;
  }

  public String getFileName() {
    return fileName;
  }

  public String classDisplayName() {
    return "ExternalFileInterface";
  }

  public Class<?> agentClass() {
    return org.nlogo.agent.Observer.class;
  }

  public String headerSource() {
    return "";
  }

  public String innerSource() {
    return "";
  }

  public void innerSource(String s) {
  }

  public String source() {
    return headerSource() + innerSource();
  }

}
