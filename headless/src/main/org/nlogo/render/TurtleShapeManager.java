// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render;

import org.nlogo.api.Graphics2DWrapper;
import org.nlogo.api.GraphicsInterface;
import org.nlogo.shape.VectorShape;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

// A lot of the code in this file is for the turtle shape cache.  The
// purpose of the cache is to speed up rendering of turtles.  We need
// to draw the same turtle shapes over and over again.  Instead of
// re-rendering the shape from scratch every time, we render it once,
// ahead of time, to a small offscreen buffer, and then when we need
// to draw it again, we can just copy the buffer to the screen.  The
// more complicated the shape is, the bigger the speedup this gets us.

// The whole caching thing gets a bit complicated because we can't
// cache an unlimited amount of bitmaps.  So we need to expire old
// entries, we only cache certain turtle sizes, etc.

public strictfp class TurtleShapeManager {
  // only cache this many pixels.  this is arbitrary.  it's a speed
  // vs. memory usage tradeoff.
  private static final int MAX_CACHE_PIXELS = 1024 * 1024 * 2;

  // this determines to what extent we ignore small changes in angle.
  // (It's a ratio because the bigger the patch size, the finer distinctions
  // will be visible.)
  private static final int ANGLE_STEP_RATIO = 30;

  private final java.util.Queue<CacheKey> cacheQueue =
      new LinkedList<CacheKey>();
  private final Map<CacheKey, CachedShape> shapeCache =
      new HashMap<CacheKey, CachedShape>();
  final org.nlogo.api.ShapeList shapeList;

  private int cellSize;
  private int pixelCount = 0;  // measured in pixels
  private int angleStep;
  private int numAngleSteps;

  TurtleShapeManager(org.nlogo.api.ShapeList shapeList) {
    this.shapeList = shapeList;
  }

  public int cacheSize() {
    return shapeCache.size();
  }

  public String getCacheReport() {
    return "shapes " + shapeCache.size() + " pixels " + pixelCount + " angleStep " + angleStep;
  }

  void resetCache(double patchSize) {
    shapeCache.clear();
    cacheQueue.clear();
    pixelCount = 0;
    cellSize = (int) StrictMath.floor(patchSize);
    if (cellSize == 0) {
      angleStep = 1;
    } else {
      angleStep = ANGLE_STEP_RATIO / cellSize;
      if (angleStep == 0) {
        angleStep = 1;
      }
    }
    numAngleSteps = 360 / angleStep;
  }

  boolean useCache(org.nlogo.api.Turtle turtle, double patchSize) {
    // ideally we'd be smart enough to cache
    // any size, but for now let's just cache
    // a few frequently used sizes - ST 1/6/05
    double turtleSize = turtle.size();
    return patchSize == cellSize &&
        (turtleSize == 1.0 ||
            turtleSize == 1.5 ||
            turtleSize == 2.0) &&
        turtle.lineThickness() == 0.0;
  }

  VectorShape getShape(org.nlogo.api.Turtle turtle) {
    return (VectorShape) shapeList.shape(turtle.shape());
  }

  CachedShape getCachedShape(VectorShape shape, java.awt.Color color,
                             double angle, double size) {
    int angleIndex;

    if (shape.isRotatable()) {
      angleIndex = (int) StrictMath.rint(angle / angleStep);
      if (angleIndex == numAngleSteps) {
        angleIndex = 0;
      }
      angle = angleIndex * angleStep;
    } else {
      angleIndex = 0;
      angle = 0.0;
    }
    CacheKey key = new CacheKey(color.getRGB(), angleIndex, shape, size);
    CachedShape cached = shapeCache.get(key);
    if (cached != null) {
      return cached;
    }
    // remove the oldest item in cache so the size stays constant
    while (pixelCount >= MAX_CACHE_PIXELS) {
      discardOldestShape();
    }
    CachedShape newShape =
        new CachedShape(shape, cellSize, (int) angle, size, color);
    shapeCache.put(key, newShape);
    cacheQueue.add(key);
    pixelCount += newShape.getPixelCount();
    return newShape;
  }

  private void discardOldestShape() {
    CacheKey oldKey = cacheQueue.remove();
    pixelCount -= shapeCache.get(oldKey).getPixelCount();
    shapeCache.remove(oldKey);
  }
}

// this might be temporary I'm going to try to make VectorShape a Drawable
// we'll see.
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

strictfp class CachedShape
    implements Drawable {
  private final java.awt.image.BufferedImage image;
  private final int imageSize; // in pixels, e.g. 5 if 5x5 (note 5 not 25)

  CachedShape(VectorShape shape, int cellSize, int angle,
              double turtleSize, java.awt.Color color) {
    imageSize = (cellSize * (int) StrictMath.ceil(turtleSize));

    image = new java.awt.image.BufferedImage
        (imageSize, imageSize,
            java.awt.image.BufferedImage.TYPE_INT_ARGB);
    Graphics2DWrapper g = new Graphics2DWrapper((java.awt.Graphics2D) image.getGraphics());
    g.antiAliasing(true);
    try {
      shape.paint(g, color, 0, 0, turtleSize, cellSize, angle, 0.0);
    } finally {
      g.dispose();
    }
  }

  public void draw(GraphicsInterface g, double size) {
    g.drawImage(image);
  }

  public double adjustSize(double turtleSize, double patchSize) {
    return 0;
  }

  int getPixelCount() {
    return imageSize * imageSize;
  }
}
