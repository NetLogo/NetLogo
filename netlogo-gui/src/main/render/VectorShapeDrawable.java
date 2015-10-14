// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render;

import org.nlogo.api.GraphicsInterface;
import org.nlogo.shape.VectorShape;

class VectorShapeDrawable
    implements Drawable {
  private final VectorShape shape;
  private final int heading;
  private final double lineThickness;
  private final double patchSize;
  private final java.awt.Color color;
  private final double turtleSize;

  VectorShapeDrawable(VectorShape shape, java.awt.Color color, double patchSize, int heading,
                      double lineThickness, double turtleSize) {
    this.shape = shape;
    this.heading = heading;
    this.lineThickness = lineThickness;
    this.patchSize = patchSize;
    this.color = color;
    this.turtleSize = turtleSize;
  }

  public void draw(GraphicsInterface g, double size) {
    shape.paint
        (g, color, 0, 0, turtleSize, patchSize, heading, lineThickness);
  }

  public double adjustSize(double turtleSize, double patchSize) {
    return 0;
  }
}
