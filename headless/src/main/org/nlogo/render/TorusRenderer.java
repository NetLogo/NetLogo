// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render;

import org.nlogo.api.GraphicsInterface;

public strictfp class TorusRenderer extends AbstractTopologyRenderer {
  public TorusRenderer(org.nlogo.api.World world) {
    super(world);
  }

  @Override
  public void fillBackground(GraphicsInterface g) {
  }

  @Override
  public void wrapDrawable(Drawable obj, GraphicsInterface g,
                           double xcor, double ycor, double turtleSize, double cellSize) {
    double size = turtleSize * cellSize + obj.adjustSize(turtleSize, cellSize);
    double offset = size / 2.0;
    double x = graphicsX(xcor, cellSize) - offset;
    double y = graphicsY(ycor, cellSize) - offset;

    g.push();
    g.translate(x, y);

    boolean wrapXRight = false;
    boolean wrapXLeft = false;

    draw(obj, g, x, y, 0, 0, size);

    if (x + size > width) {
      draw(obj, g, x, y, width, 0, size);
      wrapXRight = true;
    }
    if (x < 0) {
      draw(obj, g, x, y, -width, 0, size);
      wrapXLeft = true;
    }
    if (y + size > height) {
      draw(obj, g, x, y, 0, height, size);
      if (wrapXRight) {
        draw(obj, g, x, y, width, height, size);
      }
      if (wrapXLeft) {
        draw(obj, g, x, y, -width, height, size);
      }
    }
    if (y < 0) {
      draw(obj, g, x, y, 0, -height, size);
      if (wrapXRight) {
        draw(obj, g, x, y, width, -height, size);
      }
      if (wrapXLeft) {
        draw(obj, g, x, y, -width, -height, size);
      }
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
    int labelHeight = g.getFontMetrics().getHeight();

    g.setColor(org.nlogo.api.Color.getColor(color));

    boolean wrapXRight = false;
    boolean wrapXLeft = false;

    // draw the label in its original position
    drawLabel(label, g, x, y, 0, 0, patchSize);

    // if the label starts off the right hand side of the screen,
    // draw it on the left too.
    if (x > width) {
      drawLabel(label, g, x, y, width, 0, patchSize);
      wrapXRight = true;
    }
    // if the label falls off the left hand side of the screen,
    // draw it on the right as well.
    if (x - labelWidth < 0) {
      drawLabel(label, g, x, y, -width, 0, patchSize);
      wrapXLeft = true;
    }
    // if the label is off the bottom of the screen,
    // draw it on the top.
    if (y > height) {
      drawLabel(label, g, x, y, 0, height, patchSize);
      if (wrapXRight) {
        drawLabel(label, g, x, y, width, height, patchSize);
      }
      if (wrapXLeft) {
        drawLabel(label, g, x, y, -width, height, patchSize);
      }
    }
    // if the label starts on top and but is too big too fit,
    // it wraps to the bottom.
    // remember that 0y is at the top in java2d.
    // since labels grow up and to the left, up means a lesser y value.
    // so 0 - labelHeight should wrap to the bottom.
    if (y - labelHeight < 0) {
      drawLabel(label, g, x, y, 0, -height, patchSize);
      if (wrapXRight) {
        drawLabel(label, g, x, y, width, -height, patchSize);
      }
      if (wrapXLeft) {
        drawLabel(label, g, x, y, -width, -height, patchSize);
      }
    }
  }

  @Override
  public void paintAllPatchesBlack(GraphicsInterface g) {
    g.setColor(java.awt.Color.BLACK);
    g.fillRect(0, 0, width, height);
  }

  @Override
  public void paintViewImage(GraphicsInterface g, java.awt.Image image) {
    super.paintViewImage(g, image);

    int x = worldAndViewPreMultipliedX;
    int y = worldAndViewPreMultipliedY;

    if (worldAndViewPreMultipliedX > 0) {
      x = worldAndViewPreMultipliedX - width;
    } else if (worldAndViewPreMultipliedX < 0) {
      x = worldAndViewPreMultipliedX + width;
    }
    if (worldAndViewPreMultipliedY > 0) {
      y = worldAndViewPreMultipliedY - height;
    } else if (worldAndViewPreMultipliedY < 0) {
      y = worldAndViewPreMultipliedY + height;
    }

    boolean wrapX = false;
    boolean wrapY = false;

    if (x != 0) {
      g.drawImage(image, x, worldAndViewPreMultipliedY, width, height);
      wrapX = true;
    }
    if (y != 0) {
      g.drawImage(image, worldAndViewPreMultipliedX, y, width, height);
      wrapY = true;
    }
    if (wrapX && wrapY) {
      g.drawImage(image, x, y, width, height);
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

    boolean wrapXRight = false;
    boolean wrapXLeft = false;

    if (x2 + lineWidth > width) {
      drawable.draw(g, link, color, x1 - width, y1, x2 - width, y2, patchSize, lineThickness);
      wrapXRight = true;
    }
    if (x2 - lineWidth < 0) {
      drawable.draw(g, link, color, x1 + width, y1, x2 + width, y2, patchSize, lineThickness);
      wrapXLeft = true;
    }
    if (y2 + lineWidth > height) {
      drawable.draw(g, link, color, x1, y1 - height, x2, y2 - height, patchSize, lineThickness);

      if (wrapXRight) {
        drawable.draw(g, link, color, x1 - width, y1 - height, x2 - width, y2 - height, patchSize, lineThickness);
      }
      if (wrapXLeft) {
        drawable.draw(g, link, color, x1 + width, y1 - height, x2 + width, y2 - height, patchSize, lineThickness);
      }
    }
    if (y2 - lineWidth < 0) {
      drawable.draw(g, link, color, x1, y1 + height, x2, y2 + height, patchSize, lineThickness);

      if (wrapXRight) {
        drawable.draw(g, link, color, x1 - width, y1 + height, x2 - width, y2 + height, patchSize, lineThickness);
      }
      if (wrapXLeft) {
        drawable.draw(g, link, color, x1 + width, y1 + height, x2 + width, y2 + height, patchSize, lineThickness);
      }
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
      boolean wrapXLeft = false;
      boolean wrapXRight = false;

      if (x2 + size > width) {
        g.drawLine(x1 - width, y1, x2 - width, y2);
        wrapXRight = true;
      }
      if (x1 - size < 0) {
        g.drawLine(x1 + width, y1, x2 + width, y2);
        wrapXLeft = true;
      }
      if (y2 + size > width) {
        g.drawLine(x1, y1 - height, x2, y2 - height);
        if (wrapXRight) {
          g.drawLine(x1 - width, y1 - height, x2 - width, y2 - height);
        }
        if (wrapXLeft) {
          g.drawLine(x1 + width, y1 - height, x2 + width, y2 - height);
        }
      } else if (y1 - size < 0) {
        g.drawLine(x1, y1 + height, x2, y2 + height);
        if (wrapXRight) {
          g.drawLine(x1 - width, y1 + height, x2 - width, y2 + height);
        }
        if (wrapXLeft) {
          g.drawLine(x1 + width, y1 + height, x2 + width, y2 + height);
        }
      }
    }
  }

  @Override
  public double graphicsX(double xcor, double patchSize, double viewOffsetX) {
    return patchSize * ((xcor - world.minPxcor() + 0.5) - viewOffsetX);
  }

  @Override
  public double graphicsY(double ycor, double patchSize, double viewOffsetY) {
    return patchSize * ((-ycor + world.maxPycor() + 0.5) + viewOffsetY);
  }
}
