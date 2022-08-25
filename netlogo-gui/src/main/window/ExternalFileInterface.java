// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.core.AgentKind;
import org.nlogo.core.AgentKindJ;

public class ExternalFileInterface
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

  public void innerSource_$eq(String s) {
  }

  public String source() {
    return headerSource() + innerSource();
  }

}
