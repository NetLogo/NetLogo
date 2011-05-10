package org.nlogo.agent;

public strictfp class LinkStamp3D
    implements org.nlogo.api.LinkStamp3D {
  private final String shape;
  private final double x1;
  private final double y1;
  private final double z1;
  private final double x2;
  private final double y2;
  private final double z2;
  private final Object color;
  private final double lineThickness;
  private final boolean directedLink;
  private final double destSize;
  private final double heading;
  private final double pitch;

  LinkStamp3D(Link3D link) {
    this.shape = link.shape();
    this.x1 = link.x1();
    this.y1 = link.y1();
    this.z1 = link.z1();
    this.x2 = link.x2();
    this.y2 = link.y2();
    this.z2 = link.z2();
    this.color = link.color();
    this.lineThickness = link.lineThickness();
    this.directedLink = link.isDirectedLink();
    this.destSize = link.linkDestinationSize();
    this.heading = link.heading();
    this.pitch = link.pitch();
  }

  LinkStamp3D(String shape, double x1, double y1, double z1, double x2, double y2, double z2,
              Object color, double lineThickness, boolean directedLink, double destSize,
              double heading, double pitch) {
    this.shape = shape;
    this.x1 = x1;
    this.y1 = y1;
    this.z1 = z1;
    this.x2 = x2;
    this.y2 = y2;
    this.z2 = z2;
    this.color = color;
    this.lineThickness = lineThickness;
    this.directedLink = directedLink;
    this.destSize = destSize;
    this.heading = heading;
    this.pitch = pitch;
  }

  public String shape() {
    return shape;
  }

  public Object color() {
    return color;
  }

  public double lineThickness() {
    return lineThickness;
  }

  public double x1() {
    return x1;
  }

  public double y1() {
    return y1;
  }

  public double z1() {
    return z1;
  }

  public double x2() {
    return x2;
  }

  public double y2() {
    return y2;
  }

  public double z2() {
    return z2;
  }

  public double midpointX() {
    return (x1 + x2) / 2;
  }

  public double midpointY() {
    return (y1 + y2) / 2;
  }

  public boolean isDirectedLink() {
    return directedLink;
  }

  public double linkDestinationSize() {
    return destSize;
  }

  public double heading() {
    return heading;
  }

  public double pitch() {
    return pitch;
  }

  public double size() {
    return 0;
  }

  public boolean hasLabel() {
    return false;
  }

  public String labelString() {
    return "";
  }

  public Object labelColor() {
    return new Double(0.0);
  }

  public boolean hidden() {
    return false;
  }

  public double pointBetweenMidpointAndEnd2X(double c) {
    return 0;
  }

  public double pointBetweenMidpointAndEnd2Y(double c) {
    return 0;
  }

  public long id() {
    return 0;
  }

  public org.nlogo.api.AgentSet getBreed() {
    return null;
  }

  public org.nlogo.api.Turtle end1() {
    return null;
  }

  public org.nlogo.api.Turtle end2() {
    return null;
  }

  public int getBreedIndex() {
    return 0;
  }

  public World world() {
    return null;
  }

  public String classDisplayName() {
    return "";
  }

  public void setVariable(int vn, Object value) {
    throw new UnsupportedOperationException();
  }

  public Object getVariable(int vn) {
    throw new UnsupportedOperationException();
  }
}
