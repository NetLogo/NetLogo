// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape;

import org.nlogo.api.GraphicsInterface;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import static org.nlogo.api.Constants.ShapeWidth;

// Note: currently I think this is used only as an abstract superclass
// for Polygon.  Neither Steph nor I really knows why -- not sure if
// it's just a historical thing or if it actually makes sense that way.
//  - ST 6/11/04

// If this were to become an element type in its own right, then this
// is the method that would be used to draw it:
// drawArc( int x, int y, int width, int height, int startAngle, int arcAngle )
// except this doesn't allow for rotation.  Presumably Graphics2D has a similar
// method that would draw a rotated arc? - SAB/ST 6/11/04

public abstract strictfp class Curve
    extends Element
    implements Cloneable {

  static final long serialVersionUID = 0L;

  // Curve data members
  protected List<Integer> xcoords = new ArrayList<Integer>();
  protected List<Integer> ycoords = new ArrayList<Integer>();
  // Max and min values (used to find bounds)
  private int xmin;
  private int xmax;
  private int ymin;
  private int ymax;

  ///

  public Curve(Color c) {
    super(c);
  }

  @Override
  public void setFilled(boolean fill) {
  }    // Curves can't be filled (yet)

  /* comment this method out for now because otherwise the code won't compile
      because Curve is now abstract; this would need to be brought back if we
      started supporting Curve as its own shape type, instead of just as a
      superclass for Polygon - ST 6/11/04
      Note also that copy() has been eliminated in favor of clone(), so this
      would need to be updated for that too - ST 7/31/04
   public Element copy()
   {
     Curve newCurve = new Curve();
     newCurve.xcoords = new ArrayList( xcoords ) ;
     newCurve.ycoords = new ArrayList( ycoords ) ;

     newCurve.xmin = xmin;
     newCurve.xmax = xmax;
     newCurve.ymin = ymin;
     newCurve.ymax = ymax;

     newCurve.c = c;
     newCurve.filled = false;

     return newCurve;
   }
   */

  public Curve(Point start, Point next, Color color) {
    super(color);
    xcoords.add(Integer.valueOf(start.x));   // Begin the curve at start
    ycoords.add(Integer.valueOf(start.y));
    xcoords.add(Integer.valueOf(next.x));    // Continue it at next
    ycoords.add(Integer.valueOf(next.y));
    xmin = start.x;                      // Establish initial bounds
    xmax = start.x;
    ymin = start.y;
    ymax = start.y;
    updateBounds(next);
  }

  // NOTE: This doesn't work after the curve has been rotated
  @Override
  public java.awt.Rectangle getBounds() {
    return createRect(new Point(xmin, ymin), new Point(xmax, ymax));
  }

  // Updates the curve by adding to it the point currently occupied by the mouse
  @Override
  public void modify(Point start, Point next) {
    xcoords.add(Integer.valueOf(next.x));   // Add coords of next (start hasn't changed)
    ycoords.add(Integer.valueOf(next.y));
    updateBounds(next);
  }

  @Override
  public void draw(GraphicsInterface g, Color turtleColor, double scale, double angle) {
    int[] xArray = new int[xcoords.size()],
        yArray = new int[xcoords.size()];

    // Put the values in the coords vectors into arrays, which drawPolyLine requires
    for (int i = 0; i < xcoords.size(); ++i) {
      xArray[i] = getElt(i, xcoords);
      yArray[i] = getElt(i, ycoords);
    }

    g.setColor(getColor());
    g.drawPolyline(xArray, yArray, xcoords.size());

  }

  @Override
  public void rotateLeft() {
    // For each point in the curve
    for (int i = 0; i < xcoords.size(); ++i) {
      int temp = getElt(i, xcoords);
      xcoords.set
          (i, Integer.valueOf(getElt(i, ycoords)));
      ycoords.set
          (i, Integer.valueOf(ShapeWidth() - temp));
    }
  }

  @Override
  public void rotateRight() {
    // For each point in the curve
    for (int i = 0; i < xcoords.size(); ++i) {
      int temp = getElt(i, xcoords);
      xcoords.set
          (i, Integer.valueOf(ShapeWidth() - getElt(i, ycoords)));
      ycoords.set
          (i, Integer.valueOf(temp));
    }
  }

  @Override
  public void flipHorizontal() {
    // For each point in the curve
    for (int i = 0; i < xcoords.size(); ++i) {
      xcoords.set
          (i, Integer.valueOf(ShapeWidth() - getElt(i, xcoords)));
    }
  }

  @Override
  public void flipVertical() {
    // For each point in the curve
    for (int i = 0; i < ycoords.size(); ++i) {
      ycoords.set
          (i, Integer.valueOf(ShapeWidth() - getElt(i, ycoords)));
    }
  }

  private void updateBounds(Point newPoint) {
    xmin = StrictMath.min(xmin, newPoint.x);
    xmax = StrictMath.max(xmax, newPoint.x);
    ymin = StrictMath.min(ymin, newPoint.y);
    ymax = StrictMath.max(ymax, newPoint.y);
  }

  @Override
  public String toReadableString() {
    return "Type: Curve, color: " + c + ",\n bounds: " + getBounds();
  }

}
