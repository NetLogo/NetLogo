// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render;

import org.nlogo.api.GraphicsInterface;

public interface Drawable {
  void draw(GraphicsInterface g, double size);

  double adjustSize(double objSize, double patchSize);
}
