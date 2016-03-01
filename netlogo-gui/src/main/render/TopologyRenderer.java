// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render;

import org.nlogo.api.GraphicsInterface;
import org.nlogo.api.ViewSettings;

public interface TopologyRenderer {

  double wrapX(double pos);

  double wrapY(double pos);

  double graphicsX(double xcor, double patchSize);

  double graphicsY(double ycor, double patchSize);

  double graphicsX(double xcor, double patchSize, double viewOffsetX);

  double graphicsY(double ycor, double patchSize, double viewOffsetY);

  void prepareToPaint(ViewSettings settings, int width, int height);

  void paintViewImage(GraphicsInterface g, java.awt.Image image);

  void wrapDrawable(Drawable obj, GraphicsInterface g,
                    double xcor, double ycor, double turtleSize, double cellSize);

  void drawWrappedRect(GraphicsInterface g, java.awt.Color color, final float stroke,
                       double xcor, double ycor, double agentSize, double patchSize, final boolean fill);

  void fillBackground(GraphicsInterface g);

  void paintAllPatchesBlack(GraphicsInterface g);

  /// Links
  void drawLink(GraphicsInterface g, org.nlogo.api.Link link, LinkDrawer.LinkDrawable drawer,
                double patchSize, java.awt.Color color, double lineThickness);

  /// Drawing
  void drawLine(GraphicsInterface tg, double startX, double startY,
                double endX, double endY, double penSize);

  void drawLabelHelper(GraphicsInterface g, double xcor, double ycor, String label,
                       Object color, double patchSize, double size);
}
