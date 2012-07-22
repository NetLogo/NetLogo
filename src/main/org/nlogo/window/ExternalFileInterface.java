// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.api.AgentKind;
import org.nlogo.api.AgentKindJ;

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

  public AgentKind kind() {
    return AgentKindJ.Observer();
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
