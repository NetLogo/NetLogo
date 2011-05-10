package org.nlogo.api;

// all renderer classes deal only with GraphicsInterfaces rather
// than Graphics2D objects so we can test rendering

public interface GraphicsInterface {
  void antiAliasing(boolean on);

  void draw(java.awt.Shape shape);

  void drawImage(java.awt.image.BufferedImage image);

  void drawImage(java.awt.Image image, int x, int y, int width, int height);

  void drawLine(double x1, double y1, double x2, double y2);

  void drawLabel(String s, double x, double y, double patchSize);

  void fill(java.awt.Shape shape);

  void fillRect(int x, int y, int width, int height);

  void pop();

  void push();

  void rotate(double theta);

  void rotate(double theta, double x, double y);

  void rotate(double theta, double x, double y, double offset);

  void scale(double x, double y);

  void scale(double x, double y, double shapeWidth);

  void setColor(java.awt.Color c);

  void setComposite(java.awt.Composite comp);

  void setStroke(double width);

  void setStroke(float width, float[] dashes);

  void setStrokeFromLineThickness(double lineThickness, double scale, double cellSize, double shapeWidth);

  void translate(double x, double y);

  void setInterpolation();

  void setStrokeControl();

  void drawPolygon(int[] xcors, int ycors[], int length);

  void fillPolygon(int[] xcors, int ycors[], int length);

  void drawPolyline(int[] xcors, int ycors[], int length);

  void dispose();

  String location(double x, double y);

  void fillCircle(double x, double y, double xDiameter, double yDiameter, double scale, double angle);

  void drawCircle(double x, double y, double xDiameter, double yDiameter, double scale, double angle);

  void fillRect(double x, double y, double width, double height, double scale, double angle);

  void drawRect(double x, double y, double width, double height, double scale, double angle);

  java.awt.FontMetrics getFontMetrics();
}
