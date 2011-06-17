package org.nlogo.hubnet.mirroring;

public strictfp class Line
    extends DrawingMessage {
  static final long serialVersionUID = 0L;

  public double x1;
  public double y1;
  public double x2;
  public double y2;
  public Object color;
  public double size;
  public String mode;

  public Line() {
    super(DrawingMessage.Type.LINE);
  }

  public Line(double x1, double y1, double x2, double y2,
                    Object penColor, double penSize, String penMode) {
    super(DrawingMessage.Type.LINE);
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
    color = penColor;
    size = penSize;
    mode = penMode;
  }
}
