// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape;

import org.nlogo.api.GraphicsInterface;

import java.awt.Color;
import java.awt.Point;
import java.util.StringTokenizer;

import static org.nlogo.api.Constants.ShapeWidth;

public strictfp class Rectangle
    extends Element
    implements Cloneable {

  static final long serialVersionUID = 0L;

  public Rectangle(Point start, Point end, Color color) {
    super(color);
    upperLeft = new Point(start);
    upperRight = new Point(end.x, start.y);
    lowerLeft = new Point(start.x, end.y);
    lowerRight = new Point(end);
  }

  @Override
  public void setFilled(boolean fill) {
    filled = fill;
  }

  @Override
  public Object clone() {
    Rectangle newRect = (Rectangle) super.clone();
    newRect.upperLeft = (Point) newRect.upperLeft.clone();
    newRect.upperRight = (Point) newRect.upperRight.clone();
    newRect.lowerLeft = (Point) newRect.lowerLeft.clone();
    newRect.lowerRight = (Point) newRect.lowerRight.clone();
    return newRect;
  }

  @Override
  public java.awt.Rectangle getBounds() {
    // Determine the max and min coords, then use them to find the bounds
    setMaxsAndMins();
    return new java.awt.Rectangle(xmin, ymin, xmax - xmin, ymax - ymin);

  }

  @Override
  public boolean contains(Point p) {
    return getBounds().contains(p);
  }

  @Override
  public void modify(Point start, Point last) {
    int width = StrictMath.abs(start.x - last.x);      // Find the width and height of the shape so far
    int height = StrictMath.abs(start.y - last.y);

    upperLeft.x = StrictMath.min(start.x, last.x);    // Determine where the upper left corner is
    upperLeft.y = StrictMath.min(start.y, last.y);
    upperRight.x = upperLeft.x + width;        // Now use that, along with the width and height,
    upperRight.y = upperLeft.y;           // to figure out where the other corners are
    lowerRight.x = upperLeft.x + width;
    lowerRight.y = upperLeft.y + height;
    lowerLeft.x = upperLeft.x;
    lowerLeft.y = upperLeft.y + height;
  }

  @Override
  public void reshapeElement(Point oldPoint, Point newPoint) {
    if (modifiedPoint.equals("upperLeft")) {
      upperLeft = newPoint;
      lowerLeft.x = newPoint.x;
      upperRight.y = newPoint.y;
    }
    if (modifiedPoint.equals("upperRight")) {
      upperRight = newPoint;
      lowerRight.x = newPoint.x;
      upperLeft.y = newPoint.y;
    }
    if (modifiedPoint.equals("lowerRight")) {
      lowerRight = newPoint;
      upperRight.x = newPoint.x;
      lowerLeft.y = newPoint.y;
    }
    if (modifiedPoint.equals("lowerLeft")) {
      lowerLeft = newPoint;
      upperLeft.x = newPoint.x;
      lowerRight.y = newPoint.y;
    }

    xmin = upperLeft.x;
    xmax = upperRight.x;
    ymin = upperLeft.y;
    ymax = lowerLeft.y;
  }

  @Override
  public void moveElement(int xOffset, int yOffset) {
    upperLeft.x += xOffset;
    upperLeft.y += yOffset;
    upperRight.x += xOffset;
    upperRight.y += yOffset;
    lowerLeft.x += xOffset;
    lowerLeft.y += yOffset;
    lowerRight.x += xOffset;
    lowerRight.y += yOffset;
  }

  @Override
  public java.awt.Point[] getHandles() {
    int[] xcoords = {upperLeft.x, upperRight.x, lowerRight.x, lowerLeft.x};
    int[] ycoords = {upperLeft.y, upperRight.y, lowerRight.y, lowerLeft.y};
    // array of handles
    // order is upper left, upper right, lower right, lower left
    java.awt.Point[] handles = new java.awt.Point[4];
    for (int i = 0; i < xcoords.length; i++) {
      handles[i] = new java.awt.Point(xcoords[i], ycoords[i]);
    }
    return handles;
  }

  // Based on the current points defining the rectangle, determine the max and min x, y coords, which
  // are given to the graphics when the rectangle needs to be redrawn or deleted
  public void setMaxsAndMins() {
    int[] xcoords = {upperLeft.x, upperRight.x, lowerRight.x, lowerLeft.x};
    int[] ycoords = {upperLeft.y, upperRight.y, lowerRight.y, lowerLeft.y};

    xmin = min(xcoords);
    xmax = max(xcoords);
    ymin = min(ycoords);
    ymax = max(ycoords);
  }

  @Override
  public void draw(GraphicsInterface g, Color turtleColor,
                   double scale, double angle) {
    g.setColor(getColor(turtleColor));

    if (filled) {
      g.fillRect(upperLeft.x, upperLeft.y,
          upperRight.x - upperLeft.x, lowerLeft.y - upperLeft.y, scale, angle);
    } else {
      g.drawRect(upperLeft.x, upperLeft.y,
          upperRight.x - upperLeft.x, lowerLeft.y - upperLeft.y, scale, angle);
    }
  }

  @Override
  public void rotateLeft() {
    java.awt.Point temp = lowerLeft;
    lowerLeft = upperLeft;
    upperLeft = upperRight;
    upperRight = lowerRight;
    lowerRight = temp;
    int temp2;
    temp2 = upperLeft.x;
    upperLeft.x = upperLeft.y;
    upperLeft.y = ShapeWidth() - temp2;
    temp2 = upperRight.x;
    upperRight.x = upperRight.y;
    upperRight.y = ShapeWidth() - temp2;
    temp2 = lowerLeft.x;
    lowerLeft.x = lowerLeft.y;
    lowerLeft.y = ShapeWidth() - temp2;
    temp2 = lowerRight.x;
    lowerRight.x = lowerRight.y;
    lowerRight.y = ShapeWidth() - temp2;
  }

  @Override
  public void rotateRight() {
    java.awt.Point temp = upperLeft;
    upperLeft = lowerLeft;
    lowerLeft = lowerRight;
    lowerRight = upperRight;
    upperRight = temp;
    int temp2;
    temp2 = upperLeft.x;
    upperLeft.x = ShapeWidth() - upperLeft.y;
    upperLeft.y = temp2;
    temp2 = lowerLeft.x;
    lowerLeft.x = ShapeWidth() - lowerLeft.y;
    lowerLeft.y = temp2;
    temp2 = upperRight.x;
    upperRight.x = ShapeWidth() - upperRight.y;
    upperRight.y = temp2;
    temp2 = lowerRight.x;
    lowerRight.x = ShapeWidth() - lowerRight.y;
    lowerRight.y = temp2;
  }

  @Override
  public void flipHorizontal() {
    java.awt.Point temp = upperLeft;
    upperLeft = upperRight;
    upperRight = temp;
    temp = lowerLeft;
    lowerLeft = lowerRight;
    lowerRight = temp;
    upperLeft.x = ShapeWidth() - upperLeft.x;
    upperRight.x = ShapeWidth() - upperRight.x;
    lowerLeft.x = ShapeWidth() - lowerLeft.x;
    lowerRight.x = ShapeWidth() - lowerRight.x;
  }

  @Override
  public void flipVertical() {
    java.awt.Point temp = upperLeft;
    upperLeft = lowerLeft;
    lowerLeft = temp;
    temp = lowerRight;
    lowerRight = upperRight;
    upperRight = temp;
    upperLeft.y = ShapeWidth() - upperLeft.y;
    upperRight.y = ShapeWidth() - upperRight.y;
    lowerLeft.y = ShapeWidth() - lowerLeft.y;
    lowerRight.y = ShapeWidth() - lowerRight.y;
  }

  @Override
  public String toReadableString() {
    return "Type: Rectangle, color: " + c + ",\n bounds: " + getBounds();
  }

  @Override
  public String toString() {
    // Don't need to save <upperRight> and <lowerLeft>, because they can be derived from the other two
    return "Rectangle " + c.getRGB() + " " + filled + " " + marked + " " + upperLeft.x + " " + upperLeft.y
        + " " + lowerRight.x + " " + lowerRight.y;
  }

  // Parses text representing a rectangle element into that object
  public static org.nlogo.shape.Rectangle parseRectangle(String text) {
    StringTokenizer tokenizer = new StringTokenizer(text);

    tokenizer.nextToken();    // Skip the initial "Rectangle" identifier
    String color = tokenizer.nextToken();
    boolean b1 = tokenizer.nextToken().equals("true");
    boolean b2 = tokenizer.nextToken().equals("true");
    int x1 = Integer.valueOf(tokenizer.nextToken()).intValue();
    int y1 = Integer.valueOf(tokenizer.nextToken()).intValue();
    int x2 = Integer.valueOf(tokenizer.nextToken()).intValue();
    int y2 = Integer.valueOf(tokenizer.nextToken()).intValue();

    if (x1 == x2 && y1 == y2) {
      // the shapes editor lets you make zero-sized rectangles,
      // which are useless, so ignore them - ST 7/31/04
      return null;
    }

    org.nlogo.shape.Rectangle rect =
        new org.nlogo.shape.Rectangle(new java.awt.Point(x1, y1),
            new java.awt.Point(x2, y2),
            java.awt.Color.decode(color));
    rect.setFilled(b1);
    rect.setMarked(b2);
    return rect;
  }

  @Override
  public void setModifiedPoint(Point modified) {
    if (modified.equals(upperLeft)) {
      modifiedPoint = "upperLeft";
    } else if (modified.equals(upperRight)) {
      modifiedPoint = "upperRight";
    } else if (modified.equals(lowerRight)) {
      modifiedPoint = "lowerRight";
    } else if (modified.equals(lowerLeft)) {
      modifiedPoint = "lowerLeft";
    }

  }

  // Rectangle data members
  protected Point upperLeft,         // The 4 corners
      upperRight,
      lowerRight,
      lowerLeft;

  protected int xmin, xmax, ymin, ymax;    // The max and min x, y coords (used when redrawing/deleting)

  private String modifiedPoint;    // The point that is being dragged

  public int getX() {
    return upperLeft.x;
  }

  public int getY() {
    return upperLeft.y;
  }

  public int getWidth() {
    return lowerRight.x - upperLeft.x;
  }

  public int getHeight() {
    return lowerRight.y - upperLeft.y;
  }

  public Point[] getCorners() {
    return new Point[]{upperLeft, lowerRight};
  }
}
