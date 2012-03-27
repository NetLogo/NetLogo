// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape;

import org.nlogo.api.GraphicsInterface;

import java.awt.Color;
import java.awt.Point;
import java.util.List;

public abstract strictfp class Element
    implements java.io.Serializable, Cloneable {

  static final long serialVersionUID = 0L;
  protected boolean filled = false;

  public boolean filled() {
    return filled;
  }

  protected boolean marked = false;

  public boolean marked() {
    return marked;
  }

  public void setMarked(boolean marked) {
    this.marked = marked;
  }

  protected boolean selected = false;

  protected Color c;

  public Color getColor() {
    return c;
  }

  public void setColor(Color c) {
    this.c = c;
  }

  public Color getColor(Color turtleColor) {
    return (marked && turtleColor != null)
        ? turtleColor
        : ((turtleColor == null || c.getAlpha() == turtleColor.getAlpha()) ? c
        : new Color(c.getRed(), c.getGreen(), c.getBlue(), turtleColor.getAlpha()));
  }

  ///

  public Element(Color c) {
    this.c = c;
  }

  ///

  public abstract java.awt.Rectangle getBounds();        // Gets the element's bounds

  @Override
  public abstract String toString();                // Returns a serialized copy of the element

  public abstract String toReadableString();            // Returns a readable representation of the element

  public abstract void setFilled(boolean filled);        // Says whether the shape should be filled in

  public abstract void modify(Point start, Point last);    // Updates the element as the mouse moves

  public abstract void draw(GraphicsInterface g, Color turtleColor,   // Draws the element on the graphics object
                            double scale, double angle);

  public abstract void rotateLeft();                             // 90 degrees

  public abstract void rotateRight();                            // 90 degrees

  public abstract void flipHorizontal();

  public abstract void flipVertical();

  public abstract void setModifiedPoint(Point modified);

  @Override
  public Object clone()               // Returns a (deep, not shallow) copy of the element
  {
    try {
      return super.clone();
    } catch (CloneNotSupportedException ex) {
      // should never happen since we implement Cloneable
      throw new IllegalStateException(ex);
    }
  }

  // Updates an element whose shape or size has been altered (by the user dragging a handle).
  // Note that we don't know where which handle was dragged; each subclass of Element keeps
  // track of that its own way, for example Rectangle has a string field called
  // modifiedPoint, and Polygon has an int field called modifiedIndex - SAB/ST 6/11/04
  public abstract void reshapeElement(Point oldPoint, Point newPoint);

  // returns an array of the handle locations
  public abstract java.awt.Point[] getHandles();

  public abstract boolean contains(Point p);

  public abstract void moveElement(int xOffset, int yOffset);

  // indicates whether this Element should be saved.
  // needed for Polygon.java since we don't want to save 0 or 1 point polygons
  // - mag 9/12/03
  boolean shouldSave() {
    return true;
  }

  // Creates a rectangle from the upper-left and bottom-right corners
  protected java.awt.Rectangle createRect(Point start, Point end) {
    return new java.awt.Rectangle(
        StrictMath.min(start.x, end.x), StrictMath.min(start.y, end.y),      // Top left corner
        StrictMath.abs(start.x - end.x), StrictMath.abs(start.y - end.y));  // Width and height
  }

  // Utility for getting the ith integer from a vector of Integer objects
  protected int getElt(int i, List<Integer> v) {
    return v.get(i).intValue();
  }


  // Returns a new point resulting from <point> rotated around <pivot> clockwise by <angle>.  This is done by treating
  // <point> as a point on the circumference of a circle centered on <pivot>, and rotating it around that circle.
  protected Point rotatePoint(Point point, Point pivot, int angle) {
    // Flip the y coord of <point> around the x-axis, defined at <pivot>, to
    // adjust for graphics coordinates, where the y-axis goes down
    point = new Point(point.x, 2 * pivot.y - point.y);


    double radius = distance(point, pivot);  // The radius of the circle is the distance between the points
    if (radius == 0) {
      return point;
    }

    // Considering <pivot> to be the origin, calculate the angle between the x-axis and
    // the line formed by <pivot> and <point> - by the equation newAngle = arctan(<the slope>)

    double newAngle = StrictMath.atan((double) (point.y - pivot.y) / (double) (point.x - pivot.x));

    // Since arctan maps all slopes to the (-90, 90) range, if <point> is to the left of <pivot>,
    // the angle should be arctan(<the slope>) + 180
    if (point.x < pivot.x) {
      newAngle += StrictMath.PI;
    }

    if (newAngle < 0) {
      // Convert negative angles to positive ones (e.g. -pi/4 => 7*pi/4)
      newAngle += 2 * StrictMath.PI;
    }

    newAngle -= 2 * StrictMath.PI * angle / 360;      // Rotate <newAngle> by <angle> radians clockwise

    double newx = pivot.x + (radius * StrictMath.cos(newAngle));   // Create new coordinates for the
    double newy = pivot.y + (radius * StrictMath.sin(newAngle));   // new point on the circumference

    newy = (2 * pivot.y) - newy;   // re-adjust the y-coordinate for graphics use

    // Create the new point, making sure to
    // round instead of truncate
    return new Point((int) StrictMath.rint(newx), (int) StrictMath.rint(newy));
  }

  // Find the distance between two points
  protected double distance(Point center, Point circum) {
    return StrictMath.sqrt(StrictMath.pow(center.y - circum.y, 2.0) +
        StrictMath.pow(center.x - circum.x, 2.0));
  }

  // Rounds a double to the nearest integer
  public static int round(double d) {
    return (int) StrictMath.rint(d);
  }

  // Rounds a double to the first integer equal or greater
  protected static int ceiling(double d) {
    return (int) StrictMath.ceil(d);
  }

  // Returns the min value in the array
  protected int min(int[] array) {
    int min = array[0];

    for (int i = 1; i < array.length; ++i) {
      if (array[i] < min) {
        min = array[i];
      }
    }

    return min;
  }

  // Returns the max value in the array
  protected int max(int[] array) {
    int max = array[0];

    for (int i = 1; i < array.length; ++i) {
      if (array[i] > max) {
        max = array[i];
      }
    }

    return max;
  }

  public void select() {
    selected = true;
  }

  public void deselect() {
    selected = false;
  }

  public boolean selected() {
    return selected;
  }

}
