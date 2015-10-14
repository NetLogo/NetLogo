// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape;

import org.nlogo.api.GraphicsInterface;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

// Polygons are closed curves (i.e. the first point equals the last)
public strictfp class Polygon
    extends Curve
    implements Cloneable {

  static final long serialVersionUID = 0L;

  // these coordinates are in the global coordinate system,
  // that is, the coordinate system for the entire shape
  public List<Integer> getXcoords() {
    return xcoords;
  }

  public List<Integer> getYcoords() {
    return ycoords;
  }

  // this is used to keep track of which handle the user is dragging
  private int modifiedPointIndex;

  public Polygon(List<Integer> xcoords,
                 List<Integer> ycoords,
                 Color c) {
    super(c);
    this.xcoords = xcoords;
    this.ycoords = ycoords;
  }

  @Override
  public void setFilled(boolean fill) {
    filled = fill;
  }

  // Polygon doesn't include an end point in its constructor because it's created after the first
  // click, not after dragging
  public Polygon(Point start, Color color) {
    super(color);
    notCompleted = true;          // Until it's completed, draw with polyline
    xcoords.add(Integer.valueOf(start.x));    // Begin the polygon at start
    ycoords.add(Integer.valueOf(start.y));    // Don't need x mins and maxs (see below)
    xcoords.add(Integer.valueOf(start.x));    // Add a second point which will then be reset by
    ycoords.add(Integer.valueOf(start.y));    // moving the mouse
  }

  @Override
  public Object clone() {
    Polygon newPoly = (Polygon) super.clone();
    newPoly.xcoords = new ArrayList<Integer>(newPoly.xcoords);
    newPoly.ycoords = new ArrayList<Integer>(newPoly.ycoords);
    return newPoly;
  }

  // Because the polygon class has its own getBounds() method, let that method do the work
  @Override
  public java.awt.Rectangle getBounds() {
    int[] xArray = new int[xcoords.size()],
        yArray = new int[xcoords.size()];

    // Put the values in the coords vectors into arrays
    for (int i = 0; i < xcoords.size(); ++i) {
      xArray[i] = getElt(i, xcoords);
      yArray[i] = getElt(i, ycoords);
    }

    System.out.println("Max ycoord: " + max(yArray) + ", Min ycoord: " + min(yArray));

    return new java.awt.Polygon(xArray, yArray, xcoords.size()).getBounds();
  }

  // This is called every time the mouse is pressed
  public void addNewPoint(Point newPoint) {
    xcoords.add(Integer.valueOf(newPoint.x));  // Add the new point
    ycoords.add(Integer.valueOf(newPoint.y));
    latestIndex++;    // Set the index of the point to be modified to be this new one
  }

  // This is called when the mouse is moved, to set the last point of the polygon to <newPoint>
  public void modifyPoint(Point newPoint) {
    xcoords.set(latestIndex, Integer.valueOf(newPoint.x));   // Set the last point to be the one the
    ycoords.set(latestIndex, Integer.valueOf(newPoint.y));  // mouse is currently on
  }

  // This is never called, because Polygon functions differently from the other shapes, and so
  // requires the function above instead
  @Override
  public void modify(Point start, Point end) {
    xcoords.set(latestIndex, Integer.valueOf(end.x));
    ycoords.set(latestIndex, Integer.valueOf(end.y));
  }

  @Override
  public void reshapeElement(Point oldPoint, Point newPoint) {
    xcoords.set(modifiedPointIndex, Integer.valueOf(newPoint.x));
    ycoords.set(modifiedPointIndex, Integer.valueOf(newPoint.y));
  }

  @Override
  public void moveElement(int xOffset, int yOffset) {
    int ncoords = xcoords.size();
    for (int i = 0; i < ncoords; i++) {
      xcoords.set
          (i,
              Integer.valueOf
                  (xcoords.get(i).intValue() + xOffset));
      ycoords.set
          (i,
              Integer.valueOf
                  (ycoords.get(i).intValue() + yOffset));
    }
  }

  @Override
  public java.awt.Point[] getHandles() {
    java.awt.Point[] handles = new java.awt.Point[xcoords.size()];
    for (int i = 0; i < xcoords.size(); i++) {
      handles[i] =
          new java.awt.Point
              (xcoords.get(i).intValue(),
                  ycoords.get(i).intValue());
    }
    return handles;
  }

  @Override
  public boolean contains(Point p) {
    int ncoords = xcoords.size();

    // convert List of Integers to Object[]
    Object[] tmpXArray = xcoords.toArray();
    Object[] tmpYArray = ycoords.toArray();
    int[] xCoordArray = new int[ncoords];
    int[] yCoordArray = new int[ncoords];

    // convert Object[] to int[]
    for (int i = 0; i < ncoords; i++) {
      xCoordArray[i] = ((Integer) tmpXArray[i]).intValue();
      yCoordArray[i] = ((Integer) tmpYArray[i]).intValue();
    }

    java.awt.Polygon check = new java.awt.Polygon(xCoordArray, yCoordArray, ncoords);
    return check.contains(p);
  }

  @Override
  public void draw(GraphicsInterface g, Color turtleColor, double scale, double angle) {
    if (notCompleted)               // If it's still being created, draw it as a polyline
    {
      super.draw(g, null, scale, angle);
    } else                            // Otherwise it's done, so actually draw it as a polygon
    {
      int[] xArray = new int[xcoords.size()],
          yArray = new int[xcoords.size()];

      // Put the values in the coords vectors into arrays, which drawPolygon requires
      for (int i = 0; i < xcoords.size(); ++i) {
        xArray[i] = getElt(i, xcoords);
        yArray[i] = getElt(i, ycoords);
      }

      g.setColor(getColor(turtleColor));

      if (filled) {
        g.fillPolygon(xArray, yArray, xcoords.size());
      } else {
        g.drawPolygon(xArray, yArray, xcoords.size());
      }
    }
  }

  // This is called when the polygon has been closed and the drawing process is over
  public void finishUp() {
    xcoords.remove(latestIndex);    // Because the last point drawn and the subsequent one created
    ycoords.remove(latestIndex);    // are next to the starting point, delete them
    xcoords.remove(latestIndex - 1);
    ycoords.remove(latestIndex - 1);
    xcoords.remove(latestIndex - 2); // Also, delete the second-to-last point
    ycoords.remove(latestIndex - 2);

    notCompleted = false;
  }

  // used when the polygon has to close itself,
  // i.e. when the user doesn't finish it and clicks on another button
  public void selfClose() {
    xcoords.remove(latestIndex);
    ycoords.remove(latestIndex);

    notCompleted = false;
  }

  @Override
  public String toReadableString() {
    return "Polygon - color: " + c + ",\n          bounds: " + getBounds();
  }

  // we don't want to save a polygon that has 0 or 1 points since they
  // don't add anything to the shape and they can cause problems on windows
  // using some vm's. - mag 9/12/03
  @Override
  boolean shouldSave() {
    return xcoords.size() >= 2;
  }

  @Override
  public String toString() {
    String ret = "";              // Write the color and filled status
    ret += "Polygon " + c.getRGB() + " " + filled + " " + marked;

    for (int i = 0; i < xcoords.size(); ++i)    // Write each coordinate pair
    {
      ret += " " + getElt(i, xcoords) + " " + getElt(i, ycoords);
    }

    return ret;
  }

  // Stores the index of the set of coordinates in the polygon that are currently being updated
  public int latestIndex = 1;

  // True if the shape is still being created, indicating it should be drawn as a polyline, not a polygon
  public boolean notCompleted = false;

  // Parses text representing a polygon element into that object
  public static org.nlogo.shape.Polygon parsePolygon(String text) {
    StringTokenizer tokenizer = new StringTokenizer(text);

    tokenizer.nextToken();    // Skip the initial "Polygon" identifier
    String color = tokenizer.nextToken();
    boolean b1 = tokenizer.nextToken().equals("true");
    boolean b2 = tokenizer.nextToken().equals("true");
    List<Integer> xs = new ArrayList<Integer>();
    List<Integer> ys = new ArrayList<Integer>();

    Integer lastx = null;
    Integer lasty = null;
    while (tokenizer.hasMoreTokens()) {
      Integer newx = Integer.valueOf(tokenizer.nextToken());
      Integer newy = Integer.valueOf(tokenizer.nextToken());
      // no "point" (ha ha ha) in keeping both of two consecutive
      // identical points - ST 8/1/04
      if (!(newx.equals(lastx) && newy.equals(lasty))) {
        xs.add(newx);
        ys.add(newy);
      }
      lastx = newx;
      lasty = newy;
    }

    // the shapes editor sometimes winds up creating 0 or 1 point polygons,
    // which are useless, so ignore them - ST 8/13/03
    // or at least at one time such polygons were created -- hopefully not
    // anymore because of Steph's changes to the polygon-creation code?
    // Steph think it's not possible anymore, but in any case, we need
    // to handle files created in old versions - SAB/ST 6/11/04
    if (xs.size() < 2) {
      return null;
    }

    org.nlogo.shape.Polygon polygon =
        new org.nlogo.shape.Polygon(xs, ys, java.awt.Color.decode(color));
    polygon.setFilled(b1);
    polygon.setMarked(b2);
    return polygon;
  }

  // Polygon data members
  @Override
  public void setModifiedPoint(Point modified) {
    for (int i = 0; i < xcoords.size(); i++) {
      if ((xcoords.get(i).equals(Integer.valueOf(modified.x))) &&
          (ycoords.get(i).equals(Integer.valueOf(modified.y)))) {
        modifiedPointIndex = i;
      }
    }
  }

}
