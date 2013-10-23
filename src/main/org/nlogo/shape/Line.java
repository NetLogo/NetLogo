// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape;

import org.nlogo.api.GraphicsInterface;

import java.awt.Color;
import java.awt.Point;
import java.util.StringTokenizer;

public strictfp class Line
    extends Element
    implements Cloneable {

  static final long serialVersionUID = 0L;

  public Line(Point start, Point last, Color color) {
    super(color);
    this.start = start;
    end = last;
  }

  @Override
  public void setFilled(boolean fill) {
  }      // Lines can't be filled

  @Override
  public Object clone() {
    Line newLine = (Line) super.clone();
    newLine.start = (Point) newLine.start.clone();
    newLine.end = (Point) newLine.end.clone();
    return newLine;
  }

  @Override
  public java.awt.Rectangle getBounds() {
    return createRect(start, end);
  }

  // User just has to be close to a line for the click to contain the line
  @Override
  public boolean contains(Point p) {
    java.awt.geom.Line2D.Double check = new java.awt.geom.Line2D.Double(start, end);
    // "close" means within 3 pixels
    return (check.ptSegDist(p) < 3);
  }

  // Updates the line based on a new last coordinate obtained from the mouse
  @Override
  public void modify(Point start, Point last) {
    end.x = last.x;
    end.y = last.y;
  }

  @Override
  public void reshapeElement(Point oldPoint, Point newPoint) {
    if (modifiedPoint.equals("start")) {
      start = newPoint;
    }
    if (modifiedPoint.equals("end")) {
      end = newPoint;
    }
  }

  @Override
  public void moveElement(int xOffset, int yOffset) {
    start.x += xOffset;
    start.y += yOffset;
    end.x += xOffset;
    end.y += yOffset;
  }

  @Override
  public java.awt.Point[] getHandles() {
    return new java.awt.Point[]{start, end};
  }

  @Override
  public void rotateLeft() {
    int temp = start.x;
    start.x = start.y;
    start.y = SHAPE_WIDTH - temp;
    temp = end.x;
    end.x = end.y;
    end.y = SHAPE_WIDTH - temp;
  }

  @Override
  public void rotateRight() {
    int temp = start.x;
    start.x = SHAPE_WIDTH - start.y;
    start.y = temp;
    temp = end.x;
    end.x = SHAPE_WIDTH - end.y;
    end.y = temp;
  }

  @Override
  public void flipHorizontal() {
    start.x = SHAPE_WIDTH - start.x;
    end.x = SHAPE_WIDTH - end.x;
  }

  @Override
  public void flipVertical() {
    start.y = SHAPE_WIDTH - start.y;
    end.y = SHAPE_WIDTH - end.y;
  }

  // Draws the line defined by the start and endpoint
  @Override
  public void draw(GraphicsInterface g, Color turtleColor,
                   double scale, double angle) {
    g.setColor(getColor(turtleColor));
    g.drawLine(start.x, start.y, end.x, end.y);
  }

  public void fill(GraphicsInterface g) {
  }

  @Override
  public String toString() {
    return "Line " + c.getRGB() + " " + marked + " " + start.x + " " + start.y + " " + end.x + " " + end.y;
  }

  @Override
  public String toReadableString() {
    return "Line with color " + c + " and bounds " + getBounds();
  }

  // Parses text representing a line element into that object
  public static org.nlogo.shape.Line parseLine(String text) {
    StringTokenizer tokenizer = new StringTokenizer(text);
    tokenizer.nextToken();      // Skip the initial "Line" identifier
    String color = tokenizer.nextToken();
    boolean b1 = Boolean.valueOf(tokenizer.nextToken()).booleanValue();
    int x1 = Integer.valueOf(tokenizer.nextToken()).intValue();
    int y1 = Integer.valueOf(tokenizer.nextToken()).intValue();
    int x2 = Integer.valueOf(tokenizer.nextToken()).intValue();
    int y2 = Integer.valueOf(tokenizer.nextToken()).intValue();

    if (x1 == x2 && y1 == y2) {
      // the shapes editor lets you make zero-sized lines,
      // which are useless, so ignore them - ST 7/31/04
      return null;
    }

    org.nlogo.shape.Line line =
        new org.nlogo.shape.Line(new java.awt.Point(x1, y1),
            new java.awt.Point(x2, y2),
            java.awt.Color.decode(color));
    line.setMarked(b1);
    return line;
  }

  @Override
  public void setModifiedPoint(Point modified) {
    if (modified.equals(start)) {
      modifiedPoint = "start";
    } else if (modified.equals(end)) {
      modifiedPoint = "end";
    }
  }

  // Line data members, the start and end-points
  private Point start, end;
  private String modifiedPoint;

  public Point getStart() {
    return start;
  }

  public Point getEnd() {
    return end;
  }

}
