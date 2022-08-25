// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring;

public class HubNetDrawingMessage
    implements java.io.Serializable {
  static final long serialVersionUID = 0L;
  private final Type type;

  public enum Type {LINE, STAMP, CLEAR}

  public HubNetDrawingMessage(HubNetDrawingMessage.Type type) {
    this.type = type;
  }

  public HubNetDrawingMessage.Type getType() {
    return type;
  }
}
