// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render;

import org.nlogo.api.GraphicsInterface;

public strictfp class BoxRenderer extends AbstractTopologyRenderer {
  public BoxRenderer(org.nlogo.api.World world) {
    super(world);
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
    draw(obj, g, x, y, 0, 0, size);
    g.pop();
  }

  @Override
  public void drawLabelHelper(GraphicsInterface g, double xcor, double ycor, String label,
                              Object color, double patchSize, double size) {
    g.push();
    // find the coordinates of the bottom right corner of the
    // square enclosing area. ev 4/8/08
    double x = graphicsX(xcor + (0.5 * size), patchSize);
    double y = graphicsY(ycor - (0.5 * size), patchSize);
    g.setColor(org.nlogo.api.Color.getColor(color));
    g.drawLabel(label, x, y, patchSize);
    g.pop();
  }

  @Override
  public void fillBackground(GraphicsInterface g) {
    if (viewOffsetX != 0 || viewOffsetY != 0) {
      fillWith(g, VIEW_BACKGROUND);
    }
  }

  @Override
  public void paintAllPatchesBlack(GraphicsInterface g) {
    g.setColor(java.awt.Color.BLACK);
    g.fillRect(worldAndViewPreMultipliedX, worldAndViewPreMultipliedY, width, height);
  }

  @Override
  public void drawLink(GraphicsInterface g, org.nlogo.api.Link link, LinkDrawer.LinkDrawable drawable,
                       double patchSize, java.awt.Color color, double lineThickness) {
    int lineWidth = (int) StrictMath.max(1, (lineThickness * patchSize));

    g.setStroke(lineWidth);
    g.setColor(color);

    drawable.draw(g, link, color,
        graphicsX(link.x1(), patchSize), graphicsY(link.y1(), patchSize),
        graphicsX(link.x2(), patchSize), graphicsY(link.y2(), patchSize),
        patchSize, lineThickness);
  }

  @Override
  public void drawLine(GraphicsInterface g, double startX, double startY,
                       double endX, double endY, double penSize) {
    g.drawLine(graphicsXNoOffset(startX, world.patchSize()),
        graphicsYNoOffset(startY, world.patchSize()),
        graphicsXNoOffset(endX, world.patchSize()),
        graphicsYNoOffset(endY, world.patchSize()));
  }
}
