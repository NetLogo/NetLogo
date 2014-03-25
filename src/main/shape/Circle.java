// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape;

import org.nlogo.api.GraphicsInterface;

import java.awt.Color;
import java.awt.Point;
import java.util.StringTokenizer;

public strictfp class Circle
    extends Element
    implements Cloneable {

  static final long serialVersionUID = 0L;

  // Circle data members
  private int x;
  private int y;
  private int xDiameter;   // We need two diameters because if the element is scaled unevenly with regards
  private int yDiameter;   // to x and y, then the circle will become an ellipse

  ///

  public Circle(Point center, Point circum, Color color) {
    super(color);

    // Radius is distance from center to circumference
    double radius = distance(center, circum);
    x = center.x - round(radius);      // Top-left corner of ellipse
    y = center.y - round(radius);
    xDiameter = round(2.0 * radius);   // Diameter of ellipse
    yDiameter = xDiameter;
  }

  public Circle(int x, int y, int xDiameter, Color color) {
    super(color);
    this.x = x;
    this.y = y;
    this.xDiameter = xDiameter;
    yDiameter = xDiameter;
  }

  public java.awt.Point getOrigin() {
    return new java.awt.Point
        (x + round(xDiameter / 2),
            y + round(yDiameter / 2));
  }

  @Override
  public void setFilled(boolean fill) {
    filled = fill;
  }

  @Override
  public java.awt.Rectangle getBounds() {
    return new java.awt.Rectangle(x, y, xDiameter, yDiameter);
  }

  // Modify the circle according to where the new point on the circumference is
  @Override
  public void modify(Point center, Point circum) {
    double radius = distance(center, circum);    // The center hasn't changed, but the
    x = center.x - round(radius);                // point on the circumference has
    y = center.y - round(radius);
    xDiameter = round(2.0 * radius);
    yDiameter = xDiameter;
  }

  @Override
  public void reshapeElement(Point oldPoint, Point newPoint) {
    double change = distance(getOrigin(), newPoint);
    x = getOrigin().x - (int) change;
    y = getOrigin().y - (int) change;
    xDiameter = (int) change * 2;
    yDiameter = (int) change * 2;
  }

  @Override
  public void moveElement(int xOffset, int yOffset) {
    x += xOffset;
    y += yOffset;
  }

  @Override
  public java.awt.Point[] getHandles() {
    Point top, bottom, right, left;
    top = new Point(x + (xDiameter / 2), y);
    left = new Point(x, y + (yDiameter / 2));
    right = new Point(x + xDiameter, y + (yDiameter / 2));
    bottom = new Point(x + (xDiameter / 2), y + yDiameter);
    return new java.awt.Point[]{top, left, right, bottom};
  }

  @Override
  public boolean contains(Point p) {
    java.awt.geom.Ellipse2D.Double check = new java.awt.geom.Ellipse2D.Double(x, y, xDiameter, yDiameter);
    return check.contains(p.x, p.y);
  }

  @Override
  public void draw(GraphicsInterface g, Color turtleColor, double scale, double angle) {
    g.setColor(getColor(turtleColor));
    if (filled) {
      g.fillCircle(x, y, xDiameter, yDiameter, scale, angle);
    } else {
      g.drawCircle(x, y, xDiameter, yDiameter, scale, angle);
    }
  }

  @Override
  public void rotateLeft() {
    int oldX = x;
    x = y;
    y = SHAPE_WIDTH - oldX - yDiameter;
    int oldXDiameter = xDiameter;
    xDiameter = yDiameter;
    yDiameter = oldXDiameter;
  }

  @Override
  public void rotateRight() {
    int oldX = x;
    x = SHAPE_WIDTH - y - xDiameter;
    y = oldX;
    int oldXDiameter = xDiameter;
    xDiameter = yDiameter;
    yDiameter = oldXDiameter;
  }

  @Override
  public void flipHorizontal() {
    x = SHAPE_WIDTH - x - xDiameter;
  }

  @Override
  public void flipVertical() {
    y = SHAPE_WIDTH - y - yDiameter;
  }

  @Override
  public String toReadableString() {
    return "Type: Circle, color: " + c + ",\n bounds: " + getBounds();
  }

  @Override
  public String toString() {
    // Only save <xDiameter>, because after drawing it must equal <yDiameter>
    return "Circle " + c.getRGB() + " " + filled + " " + marked + " " + x + " " + y + " " + xDiameter;
  }

  // Parses text representing a circle element into that object
  public static org.nlogo.shape.Circle parseCircle(String text) {
    StringTokenizer tokenizer = new StringTokenizer(text);

    tokenizer.nextToken();    // Skip the initial "Circle" identifier
    String color = tokenizer.nextToken();
    boolean b1 = tokenizer.nextToken().equals("true");
    boolean b2 = tokenizer.nextToken().equals("true");
    int x1 = Integer.valueOf(tokenizer.nextToken()).intValue();
    int y1 = Integer.valueOf(tokenizer.nextToken()).intValue();
    int diam = Integer.valueOf(tokenizer.nextToken()).intValue();

    if (diam == 0) {
      // the shapes editor lets you make zero-sized circles,
      // which are useless, so ignore them - ST 7/31/04
      return null;
    }

    org.nlogo.shape.Circle circle = new org.nlogo.shape.Circle(x1, y1, diam,
        java.awt.Color.decode(color));
    circle.setFilled(b1);
    circle.setMarked(b2);
    return circle;
  }

  @Override
  public void setModifiedPoint(Point modified) {
  }

}
