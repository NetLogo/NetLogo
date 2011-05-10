package org.nlogo.agent;

import org.nlogo.api.Shape;

public strictfp class TurtleStamp3D
    implements org.nlogo.api.TurtleStamp3D {
  private final String shape;
  private final double xcor;
  private final double ycor;
  private final double zcor;
  private final double size;
  private final double heading;
  private final double pitch;
  private final double roll;
  private final Object color;
  private final double lineThickness;

  TurtleStamp3D(Turtle3D turtle) {
    shape = turtle.shape();
    xcor = turtle.xcor();
    ycor = turtle.ycor();
    zcor = turtle.zcor();
    size = turtle.size();
    heading = turtle.heading();
    pitch = turtle.pitch();
    roll = turtle.roll();
    color = turtle.color();
    lineThickness = turtle.lineThickness();
  }

  TurtleStamp3D(String shape, double xcor, double ycor, double zcor, double size,
                double heading, double pitch, double roll, double color, double lineThickness) {
    this.shape = shape;
    this.xcor = xcor;
    this.ycor = ycor;
    this.zcor = zcor;
    this.size = size;
    this.heading = heading;
    this.pitch = pitch;
    this.roll = roll;
    this.color = Double.valueOf(color);
    this.lineThickness = lineThickness;
  }

  public String shape() {
    return shape;
  }

  public Object color() {
    return color;
  }

  public double heading() {
    return heading;
  }

  public double pitch() {
    return pitch;
  }

  public double roll() {
    return roll;
  }

  public double lineThickness() {
    return lineThickness;
  }

  public double size() {
    return size;
  }

  public double xcor() {
    return xcor;
  }

  public double ycor() {
    return ycor;
  }

  public double zcor() {
    return zcor;
  }

  // stuff we're ignoring for right now
  public boolean hasLabel() {
    return false;
  }

  public String labelString() {
    return "";
  }

  public Object labelColor() {
    return null;
  }

  public boolean hidden() {
    return false;
  }

  public long id() {
    return 0;
  }

  public org.nlogo.api.AgentSet getBreed() {
    throw new UnsupportedOperationException();
  }

  public int getBreedIndex() {
    return 0;
  }

  public World world() {
    return null;
  }

  public org.nlogo.api.Protractor protractor() {
    return null;
  }

  public org.nlogo.api.Patch getPatchHere() {
    return null;
  }

  public void jump(double d) {
  }

  public void heading(double d) {
  }

  public String classDisplayName() {
    return "";
  }

  public double dx() {
    return 0;
  }

  public double dy() {
    return 0;
  }

  public double dz() {
    return 0;
  }

  public Object getVariable(int vn) {
    throw new UnsupportedOperationException();
  }

  public void setVariable(int vn, Object value) {
    throw new UnsupportedOperationException();
  }
}
