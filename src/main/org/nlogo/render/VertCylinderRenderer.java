// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render;

import org.nlogo.api.GraphicsInterface;

public strictfp class VertCylinderRenderer extends AbstractTopologyRenderer {
  public VertCylinderRenderer(org.nlogo.api.World world) {
    super(world);
  }

  @Override
  public void fillBackground(GraphicsInterface g) {
    if (worldAndViewPreMultipliedX != 0) {
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

    if (x + size > width) {
      draw(obj, g, x, y, width, 0, size);
    }
    if (x < 0) {
      draw(obj, g, x, y, -width, 0, size);
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

    int labelWidth = g.getFontMetrics().stringWidth(label);

    g.setColor(org.nlogo.api.Color.getColor(color));

    // draw the label in its original position
    drawLabel(label, g, x, y, 0, 0, patchSize);

    // if the label starts off the right hand side of the screen,
    // draw it on the left too.
    if (x > width) {
      drawLabel(label, g, x, y, width, 0, patchSize);
    }
    // if the label falls off the left hand side of the screen,
    // draw it on the right as well.
    if (x - labelWidth < 0) {
      drawLabel(label, g, x, y, -width, 0, patchSize);
    }
  }

  @Override
  public void paintAllPatchesBlack(GraphicsInterface g) {
    g.setColor(java.awt.Color.BLACK);
    g.fillRect(0, worldAndViewPreMultipliedY, width, height);
  }

  @Override
  public void paintViewImage(GraphicsInterface g, java.awt.Image image) {
    super.paintViewImage(g, image);

    int x = worldAndViewPreMultipliedX;

    if (worldAndViewPreMultipliedX > 0) {
      x = worldAndViewPreMultipliedX - width;
    } else if (worldAndViewPreMultipliedX < 0) {
      x = worldAndViewPreMultipliedX + width;
    }

    if (x != 0) {
      g.drawImage(image, x, worldAndViewPreMultipliedY, width, height);
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

    if (x2 + lineWidth > width) {
      drawable.draw(g, link, color, x1 - width, y1, x2 - width, y2,
          patchSize, lineThickness);
    }
    if (x2 - lineWidth < 0) {
      drawable.draw(g, link, color, x1 + width, y1, x2 + width, y2,
          patchSize, lineThickness);
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
      if (x2 + size > width) {
        g.drawLine(x1 - width, y1, x2 - width, y2);
      }
      if (x1 - size < 0) {
        g.drawLine(x1 + width, y1, x2 + width, y2);
      }
    }
  }

  @Override
  public double graphicsX(double xcor, double patchSize, double viewOffsetX) {
    return patchSize * ((xcor - world.minPxcor() + 0.5) - viewOffsetX);
  }
}
