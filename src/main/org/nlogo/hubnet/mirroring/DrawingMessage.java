package org.nlogo.hubnet.mirroring;

public strictfp class DrawingMessage
    implements java.io.Serializable {
  static final long serialVersionUID = 0L;
  private final Type type;

  public enum Type {LINE, STAMP, CLEAR}

  public DrawingMessage(Type type) {
    this.type = type;
  }

  public Type getType() {
    return type;
  }
}
