// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring;

public strictfp class TurtleStamp
    extends DrawingMessage {
  static final long serialVersionUID = 0L;

  public double xcor;
  public double ycor;
  public String shape;
  public Object color;
  public double heading;
  public double size;
  public boolean hidden;
  public double lineThickness;
  public boolean erase;

  public TurtleStamp() {
    super(DrawingMessage.Type.STAMP);
  }

  public TurtleStamp(org.nlogo.api.Turtle turtle, boolean erase) {
    super(DrawingMessage.Type.STAMP);
    xcor = turtle.xcor();
    ycor = turtle.ycor();
    shape = turtle.shape();
    color = turtle.color();
    heading = turtle.heading();
    size = turtle.size();
    hidden = turtle.hidden();
    lineThickness = turtle.lineThickness();
    this.erase = erase;
  }
}
