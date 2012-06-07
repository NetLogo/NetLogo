// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring;

public strictfp class HubNetDrawingMessage
    implements java.io.Serializable {
  static final long serialVersionUID = 0L;
  private final Type type;

  public enum Type {LINE, STAMP, CLEAR}

  public HubNetDrawingMessage(Type type) {
    this.type = type;
  }

  public Type getType() {
    return type;
  }
}
