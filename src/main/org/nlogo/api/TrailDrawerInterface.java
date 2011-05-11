package org.nlogo.api;

public interface TrailDrawerInterface
    extends DrawingInterface {
  void drawLine(double x0, double y0, double x1, double y1,
                Object color, double size, String mode);

  void setColors(int[] colors);

  Object getDrawing();

  boolean sendPixels();

  void sendPixels(boolean dirty);

  void stamp(Agent agent, boolean erase);

  void readImage(java.io.InputStream is) throws java.io.IOException;

  void importDrawing(File file) throws java.io.IOException;

  java.awt.image.BufferedImage getAndCreateDrawing(boolean dirty);

  void clearDrawing();

  void exportDrawingToCSV(java.io.PrintWriter writer);

  void rescaleDrawing();
}
