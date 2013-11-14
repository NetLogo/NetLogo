// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render;

import org.nlogo.api.GraphicsInterface;

public strictfp class HorizCylinderRenderer extends AbstractTopologyRenderer {
  public HorizCylinderRenderer(org.nlogo.api.World world) {
    super(world);
  }

  @Override
  public void fillBackground(GraphicsInterface g) {
    if (viewOffsetX != 0) {
      fillWith(g, VIEW_BACKGROUND);
    }
  }

  @Override
  public void wrapDrawable(Drawable obj, GraphicsInterface g, double xcor, double ycor,
                           double turtleSize, double cellSize) {
    double size = turtleSize * cellSize + obj.adjustSize(turtleSize, cellSize);
    double offset = size / 2.0;
    double x = graphicsX(xcor, cellSize) - offset;
    double y = graphicsY(ycor, cellSize) - offset;

    g.push();
    g.translate(x, y);
    draw(obj, g, x, y, 0, 0, size);

    if (y + size > height) {
      draw(obj, g, x, y, 0, height, size);
    }
    if (y < 0) {
      draw(obj, g, x, y, 0, -height, size);
    }
    g.pop();
  }

  @Override
  public void drawLabelHelper(GraphicsInterface g, double xcor, double ycor, String label,
                              Object color, double patchSize, double size) {
    // find the coordinates of the bottom right corner of the
    // square enclosing area. ev 4/8/08
    double x = graphicsX(xcor + (0.5 * size), patchSize);
    double y = graphicsY(ycor - (0.5 * size), patchSize);

    int labelHeight = g.getFontMetrics().getHeight();

    g.setColor(org.nlogo.api.Color.getColor(color));

    // draw the label in its original position
    drawLabel(label, g, x, y, 0, 0, patchSize);

    // if the label is off the bottom of the screen,
    // draw it on the top.
    if (y > height) {
      drawLabel(label, g, x, y, 0, height, patchSize);
    }
    // if the label starts on top and but is too big too fit,
    // it wraps to the bottom.
    // remember that 0y is at the top in java2d.
    // since labels grow up and to the left, up means a lesser y value.
    // so 0 - labelHeight should wrap to the bottom.
    if (y - labelHeight < 0) {
      drawLabel(label, g, x, y, 0, -height, size);
    }
  }

  @Override
  public void paintAllPatchesBlack(GraphicsInterface g) {
    g.setColor(java.awt.Color.BLACK);
    g.fillRect(worldAndViewPreMultipliedX, 0, width, height);
  }

  @Override
  public void paintViewImage(GraphicsInterface g, java.awt.Image image) {
    super.paintViewImage(g, image);

    int y = worldAndViewPreMultipliedY;

    if (worldAndViewPreMultipliedY > 0) {
      y = worldAndViewPreMultipliedY - height;
    } else if (worldAndViewPreMultipliedY < 0) {
      y = worldAndViewPreMultipliedY + height;
    }

    if (y != 0) {
      g.drawImage(image, worldAndViewPreMultipliedX, y, width, height);
    }
  }

  @Override
  public void drawLink(GraphicsInterface g, org.nlogo.api.Link link, LinkDrawer.LinkDrawable drawable,
                       double patchSize, java.awt.Color color, double lineThickness) {
    double x1 = graphicsX(link.x1(), patchSize);
    double y1 = graphicsY(link.y1(), patchSize);
    double x2 = graphicsX(link.x2(), patchSize);
    double y2 = graphicsY(link.y2(), patchSize);
    int lineWidth = (int) StrictMath.max(1, (lineThickness * patchSize));

    g.setStroke(lineWidth);
    g.setColor(color);

    drawable.draw(g, link, color, x1, y1, x2, y2, patchSize, lineThickness);

    if (y2 + lineWidth > height) {
      drawable.draw(g, link, color, x1, y1 - height, x2, y2 - height, patchSize, lineThickness);
    }
    if (y2 - lineWidth < 0) {
      drawable.draw(g, link, color, x1, y1 + height, x2, y2 + height, patchSize, lineThickness);
    }
  }

  @Override
  public void drawLine(GraphicsInterface g, double startX, double startY,
                       double endX, double endY, double penSize) {
    double x1 = graphicsXNoOffset(startX, world.patchSize());
    double y1 = graphicsYNoOffset(startY, world.patchSize());
    double x2 = graphicsXNoOffset(endX, world.patchSize());
    double y2 = graphicsYNoOffset(endY, world.patchSize());

    double size = penSize / 2;

    g.drawLine(x1, y1, x2, y2);

    if (penSize > 1) {
      if (y2 + size > width) {
        g.drawLine(x1, y1 - height, x2, y2 - height);
      } else if (y1 - size < 0) {
        g.drawLine(x1, y1 + height, x2, y2 + height);
      }
    }
  }

  @Override
  public double graphicsY(double ycor, double patchSize, double viewOffsetY) {
    return patchSize * ((-ycor + world.maxPycor() + 0.5) + viewOffsetY);
  }
}
