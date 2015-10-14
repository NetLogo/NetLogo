// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape;

import org.nlogo.api.GraphicsInterface;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public strictfp class LinkShape
    implements org.nlogo.api.Shape, Cloneable, java.io.Serializable, DrawableShape {
  static final long serialVersionUID = 0L;

  private VectorShape directionIndicator;
  private double curviness;
  private String name;

  public LinkShape() {
    name = "";
    directionIndicator = getDefaultLinkDirectionShape();
    lines[0] = new LinkLine(-0.2, false);
    lines[1] = new LinkLine(0.0, true);
    lines[2] = new LinkLine(0.2, false);
  }

  private LinkLine[] lines = new LinkLine[3];

  public LinkLine getLine(int i) {
    return lines[i];
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setLineVisible(int index, boolean visible) {
    lines[index].setVisible(visible);
  }

  public void add(int index, LinkLine line) {
    lines[index] = line;
  }

  public void setDashiness(int index, float[] dashes) {
    lines[index].setDashiness(dashes);
  }

  public float[] getDashes(int index) {
    return lines[index].getDashes();
  }

  public double curviness() {
    return curviness;
  }

  public void curviness(double curviness) {
    this.curviness = curviness;
  }

  public String dashinessString(int index) {
    return lines[index].dashinessString();
  }

  public void setDashes(int index, String str) {
    lines[index].setDashes(str);
  }

  public boolean isTooSimpleToPaint() {
    return !lines[0].isVisible() && !lines[2].isVisible() &&
        curviness == 0 && lines[1].isStraightPlainLine();
  }

  public org.nlogo.api.Shape getDirectionIndicator() {
    return directionIndicator;
  }

  public void setDirectionIndicator(VectorShape shape) {
    directionIndicator = shape;
  }

  public void paint(GraphicsInterface g, java.awt.Color color,
                    int x, int y, double cellSize, int angle) {
    paint(g, color, x, y, cellSize / 2, 2, angle, 0, 0, true);
  }

  public void paint(GraphicsInterface g, java.awt.Color color,
                    double x, double y, double cellSize, double size,
                    int angle, double lineThickness, double destSize, boolean isDirected) {
    double aR = StrictMath.toRadians(angle);
    double aSin = StrictMath.sin(aR) * size * cellSize;
    double aCos = StrictMath.cos(aR) * size * cellSize;
    paint(g, color,
        x + aSin + (cellSize * size / 2), y + aCos,
        x + (cellSize * size / 2), y,
        cellSize, size, lineThickness, destSize, isDirected);
  }

  public void paint(GraphicsInterface g, java.awt.Color color,
                    double x1, double y1, double x2, double y2,
                    double cellSize, double size, double lineThickness,
                    double destSize, boolean isDirected) {
    for (int i = 0; i < lines.length; i++) {
      if (lines[i].isVisible()) {

        float lt = (float) StrictMath.max(1, cellSize * lineThickness);
        Shape shape = lines[i].getShape(x1, y1, x2, y2, curviness, size, cellSize, lt);
        lines[i].paint(g, color, cellSize, lt, shape);
      }
    }

    if (isDirected) {
      Shape arc = lines[1].getShape(x2, y2, x1, y1, -curviness, size, cellSize, 1);
      paintDirectionIndicator(g, color, arc, cellSize, lineThickness, size, destSize + 1);
    }
  }

  public double[] getDirectionIndicatorTransform(double x1, double y1, double x2, double y2,
                                                 double linkLength, double destSize,
                                                 org.nlogo.api.Link link,
                                                 double cellSize, double size) {
    if (curviness == 0) {
      double[] comps = new double[3];
      comps[0] = link.heading();
      comps[1] = x2 + ((x1 - x2) / linkLength * destSize * 2 / 3);
      comps[2] = y2 - ((y2 - y1) / linkLength * destSize * 2 / 3);
      return comps;
    } else {
      Shape arc = lines[1].getShape(x2, y2, x1, y1, curviness * 3,
          linkLength, cellSize, 1);
      double[] trans = getDirectionIndicatorTransform
          (arc, getDestShape(arc, destSize, cellSize));
      trans[0] = -trans[0] + 180;
      return trans;
    }
  }

  // when there is no curviness we don't need to do the whole spliting path in
  // half thing ev 5/22/07
  public void paintDirectionIndicator(GraphicsInterface g, java.awt.Color color,
                                      double x1, double y1, double x2, double y2,
                                      double heading, double cellSize, double lineThickness,
                                      double destSize, double linkLength) {
    double xcomp = (x1 - x2) / linkLength * destSize * 2 / 3;
    double ycomp = (y2 - y1) / linkLength * destSize * 2 / 3;
    double xmid = (x1 - x2) / 2;
    double ymid = (y2 - y1) / 2;

    // we don't want the direction indicator to ever be closer to the source than the destination
    // so if we end up past the midpoint revert to the midpoint ev  5/8/09
    if (StrictMath.abs(xmid) < StrictMath.abs(xcomp) && StrictMath.abs(ymid) < StrictMath.abs(ycomp)) {
      xcomp = xmid;
      ycomp = ymid;
    }

    double scaleFactor = directionIndicatorScale(lineThickness, cellSize);

    directionIndicator.paint
        (g, color, x2 + xcomp - (cellSize * scaleFactor / 2),
            y2 - ycomp - (cellSize * scaleFactor / 2),
            scaleFactor, cellSize, (int) heading, lineThickness);
  }

  public int numLines() {
    int numLines = 0;
    for (int i = 0; i < lines.length; i++) {
      if (lines[i].isVisible()) {
        numLines++;
      }
    }
    return numLines;
  }

  public void paintDirectionIndicator(GraphicsInterface g, java.awt.Color color,
                                      Shape arc, double cellSize, double lineThickness,
                                      double size, double destSize) {
    double[] trans = getDirectionIndicatorTransform
        (arc, getDestShape(arc, destSize, cellSize));
    double scale = directionIndicatorScale(lineThickness, cellSize);
    directionIndicator.paint(g, color, trans[1] - (cellSize * scale / 2),
        trans[2] - (cellSize * scale / 2),
        scale, cellSize, (int) trans[0],
        lineThickness);
  }

  // there is no hidden meaning in this formula I got it through trial and error
  // ev 6/7/07
  private double directionIndicatorScale(double lineThickness, double cellSize) {
    return (lineThickness * StrictMath.sqrt(cellSize / 2) + 2) *
        StrictMath.max(1, numLines() / 1.5);
  }

  public Shape getDestShape(Shape arc, double size, double cellSize) {
    PathIterator i = arc.getPathIterator(null, 1);
    double[] p = new double[6];
    i.currentSegment(p);
    AffineTransform trans =
        AffineTransform.getTranslateInstance(p[0], p[1]);
    trans.scale(cellSize, cellSize);
    trans.scale(size, size);
    return trans.createTransformedShape
        (new java.awt.geom.Ellipse2D.Double(-0.5, -0.5, 1, 1));
  }

  public double[] getDirectionIndicatorTransform(Shape arc, Shape dest) {
    double[] pts = new double[6];
    Point2D p1 = null;
    Point2D p2 = null;
    double[] trans = new double[3];
    for (PathIterator i = arc.getPathIterator(null, 1); !i.isDone(); i.next()) {
      int ret = i.currentSegment(pts);
      if (ret == PathIterator.SEG_MOVETO) {
        p2 = new Point2D.Double(pts[0], pts[1]);
      } else if (ret == PathIterator.SEG_LINETO) {
        p1 = p2;
        p2 = new Point2D.Double(pts[0], pts[1]);
        if (dest.contains(p1) && !dest.contains(p2)) {
          trans = getDirectionIndicatorTransform(new Line2D.Double(p1, p2), dest);
          break;
        }
      }
    }
    return trans;
  }

  public double[] getDirectionIndicatorTransform(Line2D line, Shape dest) {
    double dx = line.getX1() - line.getX2();
    double dy = line.getY1() - line.getY2();
    while ((dx * dx + dy * dy) > 1) {
      line = getLastOutsideSegment(line, dest);
      dx = line.getX1() - line.getX2();
      dy = line.getY1() - line.getY2();
    }

    double angle = (270 + StrictMath.toDegrees
        (StrictMath.PI + StrictMath.atan2(dy, dx)))
        % 360;
    return new double[]{angle, line.getX1(), line.getY1()};
  }

  protected Line2D getLastOutsideSegment(Line2D line, Shape dest) {
    Line2D left = new Line2D.Double();
    Line2D right = new Line2D.Double();
    do {
      split(line, left, right);
      line = left;
    } while (!dest.contains(line.getP2()));

    return right;
  }

  protected void split(Line2D src, Line2D left, Line2D right) {
    double x1 = src.getX1();
    double y1 = src.getY1();
    double x2 = src.getX2();
    double y2 = src.getY2();

    double mx = x1 + (x2 - x1) / 2.0;
    double my = y1 + (y2 - y1) / 2.0;
    left.setLine(x1, y1, mx, my);
    right.setLine(mx, my, x2, y2);
  }

  public boolean isRotatable() {
    return true;
  }

  public int getEditableColorIndex() {
    return 0;
  }

  @Override
  public Object clone() {
    LinkShape newShape;
    try {
      newShape = (LinkShape) super.clone();
    } catch (CloneNotSupportedException ex) {
      throw new IllegalStateException(ex);
    }
    newShape.directionIndicator = (VectorShape) directionIndicator.clone();
    newShape.lines = new LinkLine[lines.length];
    for (int i = 0; i < lines.length; i++) {
      newShape.lines[i] = (LinkLine) lines[i].clone();
    }
    return newShape;
  }

  @Override
  public String toString() {
    String str = name + "\n" + curviness + "\n";
    for (int i = 0; i < lines.length; i++) {
      str += lines[i].toString() + "\n";
    }
    str += directionIndicator.toString();
    return str;
  }

  public static List<org.nlogo.api.Shape> parseShapes(String[] shapes, String version) {
    int index = 0;
    List<org.nlogo.api.Shape> ret =
        new ArrayList<org.nlogo.api.Shape>();
    LinkShape shape;

    // Skip initial blank lines, if any
    while ((shapes.length > index) &&
        (0 == VectorShape.getString(shapes, index).length())) {
      index++;
    }

    // Go through the lines of text, reading in shapes
    while (shapes.length > index) {
      shape = new LinkShape();
      index = parseShape(shapes, version, shape, index);
      ret.add(shape);     // Add the shape to the return vector
      index++;         // Skip the blank line we're on before looking for the next shape
    }
    return ret;
  }

  public static int parseShape(String[] shapes, String version, LinkShape shape, int index) {
    LinkLine line;
    shape.setName(VectorShape.getString(shapes, index++));

    shape.curviness = Double.parseDouble(VectorShape.getString(shapes, index++));

    for (int i = 0; i < 3; i++) {
      line = new LinkLine();
      index = LinkLine.parseLine(shapes, version, line, index);
      shape.add(i, line);
    }

    VectorShape indicator = new VectorShape();
    index = VectorShape.parseShape(shapes, version, indicator, index);
    shape.setDirectionIndicator(indicator);

    return index;
  }

  public static LinkShape getDefaultLinkShape() {
    LinkShape result = new LinkShape();
    result.setName("default");
    result.setDirectionIndicator(getDefaultLinkDirectionShape());
    return result;
  }

  public static VectorShape getDefaultLinkDirectionShape() {
    VectorShape result = new VectorShape();
    result.setName("link direction");
    result.setRotatable(true);
    result.setEditableColorIndex(0);
    result.addElement
        ("Line -7500403 true 150 150 90 180");
    result.addElement
        ("Line -7500403 true 150 150 210 180");
    return result;
  }
}
