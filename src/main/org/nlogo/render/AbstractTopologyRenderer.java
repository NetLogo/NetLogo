// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render;

import org.nlogo.api.GraphicsInterface;
import org.nlogo.api.ViewSettings;

public abstract strictfp class AbstractTopologyRenderer implements TopologyRenderer {
  final org.nlogo.api.World world;

  public AbstractTopologyRenderer(org.nlogo.api.World world) {
    this.world = world;
  }

  /// bookkeeping
  /// these values are cached for speed and also so we don't have to
  /// pass them into every single draw method.  However, be very very
  /// careful to update these values when you start rendering in a new
  /// view.  ev 6/23/09
  int width;
  int height;

  int viewWidth;
  int viewHeight;

  int worldAndViewPreMultipliedX;
  int worldAndViewPreMultipliedY;
  double viewOffsetX;
  double viewOffsetY;

  public void prepareToPaint(ViewSettings settings, int width, int height) {
    this.width = width;
    this.height = height;
    viewWidth = (int) StrictMath.round(settings.viewWidth() * settings.patchSize());
    viewHeight = (int) StrictMath.round(settings.viewHeight() * settings.patchSize());

    if (settings.renderPerspective()) {
      viewOffsetX = settings.viewOffsetX();
      viewOffsetY = settings.viewOffsetY();
      worldAndViewPreMultipliedX = -(int) StrictMath.round(settings.patchSize() * viewOffsetX);
      worldAndViewPreMultipliedY = (int) StrictMath.round(settings.patchSize() * viewOffsetY);
    } else {
      worldAndViewPreMultipliedX = 0;
      worldAndViewPreMultipliedY = 0;
      viewOffsetX = 0;
      viewOffsetY = 0;
    }
  }

  // Drawables
  void draw(Drawable drawable, GraphicsInterface g, double x, double y, double xOffset, double yOffset, double size) {
    if (x + size - xOffset >= 0 && x - xOffset <= viewWidth &&
        y + size - yOffset >= 0 && y - yOffset <= viewHeight) {
      g.push();
      g.translate(-xOffset, -yOffset);
      drawable.draw(g, size);
      g.pop();
    }
  }

  protected void drawLabel(String label, GraphicsInterface g, double x, double y,
                           double xOffset, double yOffset, double patchSize) {
    g.push();
    g.drawLabel(label, x - xOffset, y - yOffset, patchSize);
    g.pop();
  }

  public void drawWrappedRect(GraphicsInterface g, java.awt.Color color, final float stroke,
                              double xcor, double ycor, double agentSize, double patchSize, final boolean fill) {
    g.setColor(color);
    wrapDrawable
        (new Drawable() {
          public void draw(GraphicsInterface g, double size) {
            g.setStroke(stroke);
            java.awt.geom.Rectangle2D rect =
                new java.awt.geom.Rectangle2D.Double(0, 0, size, size);
            if (fill) {
              g.fill(rect);
            } else {
              g.draw(rect);
            }
          }

          public double adjustSize(double turtleSize, double patchSize) {
            return patchSize >= 5.0 ? -2 : 0;
          }
        },
            g, xcor, ycor, agentSize, patchSize);
  }

  /// Patches/ images layers

  void fillWith(GraphicsInterface g, java.awt.Color color) {
    g.setColor(color);
    g.fillRect(0, 0, width, height);
  }

  public void paintViewImage(GraphicsInterface g, java.awt.Image image) {
    g.setInterpolation();
    g.drawImage(image, worldAndViewPreMultipliedX, worldAndViewPreMultipliedY, width, height);
  }

  /// Wrapping

  public double wrapX(double pos) {
    return world.wrap(pos, world.minPxcor() - 0.5, world.maxPxcor() + 0.5);
  }

  public double wrapY(double pos) {
    return world.wrap(pos, world.minPycor() - 0.5, world.maxPycor() + 0.5);
  }

  /// coordinate system translation

  // since the drawing is a bitmap that we just wrap as
  // a whole we don't need to take the offset into account
  // when we draw the line. ev 3/12/08
  double graphicsXNoOffset(double xcor, double patchSize) {
    return patchSize * (xcor - (world.minPxcor() - 0.5)) - 0.5;
  }

  double graphicsYNoOffset(double ycor, double patchSize) {
    return patchSize * (-ycor + (world.maxPycor() + 0.5)) - 0.5;
  }

  final public double graphicsX(double xcor, double patchSize) {
    return graphicsX(xcor, patchSize, viewOffsetX);
  }

  public double graphicsX(double xcor, double patchSize, double viewOffsetX) {
    return patchSize * ((xcor - world.minPxcor() + 0.5) - viewOffsetX);
  }

  final public double graphicsY(double ycor, double patchSize) {
    return graphicsY(ycor, patchSize, viewOffsetY);
  }

  public double graphicsY(double ycor, double patchSize, double viewOffsetY) {
    return patchSize * ((-(ycor) + world.maxPycor() + 0.5) + viewOffsetY);
  }
}
