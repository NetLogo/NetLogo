// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render;

import org.nlogo.api.Dump;
import org.nlogo.api.Graphics2DWrapper;

public strictfp class TrailDrawer
    implements org.nlogo.api.TrailDrawerInterface,
    org.nlogo.api.ViewSettings {
  private final org.nlogo.api.World world;
  private java.awt.image.BufferedImage drawingImage = null;
  private int width;
  private int height;
  int[] colors = null;
  public TopologyRenderer topology;
  private final TurtleDrawer turtleDrawer;
  private final LinkDrawer linkDrawer;

  public TrailDrawer(org.nlogo.api.World world, TurtleDrawer turtleDrawer, LinkDrawer linkDrawer) {
    this.world = world;
    this.turtleDrawer = turtleDrawer;
    this.linkDrawer = linkDrawer;
  }

  // for rendering drawing in 3D view
  boolean drawingDirty = false;
  boolean drawingBlank = true;

  public int[] colors() {
    if (drawingDirty) {
      if (colors == null) {
        colors = new int[width * height * 4];
      }
      java.awt.image.Raster raster = drawingImage.getRaster();
      // note that the docs say the output array should be allocated
      // by getDataElements if it is null, my experience is it doesn't. ev 9/21/05
      raster.getDataElements(0, 0, width, height, colors);
    }

    return colors;
  }

  public void setTopology(TopologyRenderer topology) {
    this.topology = topology;
  }

  public boolean sendPixels() {
    return sendPixels;
  }

  public void sendPixels(boolean dirty) {
    sendPixels = dirty;
  }

  private boolean sendPixels = false;

  public boolean isDirty() {
    return drawingDirty;
  }

  public boolean isBlank() {
    return drawingBlank;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public void markDirty() {
    drawingBlank = false;
    drawingDirty = true;
  }

  public void markClean() {
    drawingDirty = false;
  }

  private void setUpDrawingImage() {
    width = (int) StrictMath.round(world.patchSize() * world.worldWidth());
    height = (int) StrictMath.round(world.patchSize() * world.worldHeight());

    if (width > 0 && height > 0) {
      drawingImage =
          new java.awt.image.BufferedImage
              (width, height,
                  java.awt.image.BufferedImage.TYPE_INT_ARGB);
      colors = null;
    } else {
      drawingImage = null;
    }

    drawingBlank = true;
  }

  public void rescaleDrawing() {
    java.awt.image.BufferedImage oldImage = drawingImage;

    setUpDrawingImage();

    if (oldImage != null && drawingImage != null) {
      java.awt.Graphics2D graphics = drawingImage.createGraphics();

      graphics.drawImage(oldImage, 0, 0, width, height, null);

      drawingDirty = true;
    }
  }


  // sometimes we want to make sure that a drawing is created
  // (like for the api method getDrawing)
  // and sometimes we only want to get the drawing if it has already
  // been created (like for the 3D view or hubnet view mirroring)
  // in which case it's ok to return null.
  // getAndCreateDrawing is the only method available from the api
  // ev 7/31/06
  public java.awt.image.BufferedImage getAndCreateDrawing(boolean dirty) {
    if (drawingImage == null) {
      setUpDrawingImage();
    }
    if (dirty) {
      drawingBlank = false;
      drawingDirty = true;
    }

    return drawingImage;
  }

  public Object getDrawing() {
    return drawingImage;
  }

  // for hubnet client
  public void readImage(java.io.InputStream is)
      throws java.io.IOException {
    setUpDrawingImage();

    java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(is);

    java.awt.Graphics2D dg = drawingImage.createGraphics();

    dg.drawImage(image, 0, 0, null);

    drawingBlank = false;
    drawingDirty = true;
  }

  public void importDrawing(java.io.InputStream is)
      throws java.io.IOException {
    if (drawingImage == null) {
      setUpDrawingImage();
    }
    if (drawingImage != null) {

      java.awt.image.BufferedImage image;

      image = javax.imageio.ImageIO.read(is);

      if (image == null) {
        throw new javax.imageio.IIOException("Unsupported image format.");
      }
      float scalex = (float) getWidth() / (float) image.getWidth();
      float scaley = (float) getHeight() / (float) image.getHeight();
      float scale = scalex < scaley ? scalex : scaley;
      java.awt.image.BufferedImage scaledImage = null;

      if (scale != 1) {

        final java.awt.image.AffineTransformOp trans =
            new java.awt.image.AffineTransformOp(java.awt.geom.AffineTransform.getScaleInstance(scale, scale),
                java.awt.image.AffineTransformOp.TYPE_BILINEAR);

        // To workaround a java bug, if our image was read
        // into a grayscale color space BufferedImage, than we
        // want to make sure we scale to the same color model
        // so that the colors don't get brightened.  However,
        // we can't do this for image buffers with alpha
        // values, or the scaling gets hosed too.  A curse
        // upon all "open source" languages with closed source
        // implementations. -- CLB
        if (image.getColorModel().getColorSpace().getType() == java.awt.color.ColorSpace.TYPE_GRAY
            && !image.getColorModel().hasAlpha()) {
          scaledImage = trans.createCompatibleDestImage(image, image.getColorModel());
          trans.filter(image, scaledImage);
        } else {
          scaledImage = trans.filter(image, null);
        }

      } else {
        scaledImage = image;
      }

      final int xOffset = (getWidth() - scaledImage.getWidth()) / 2;
      final int yOffset = (getHeight() - scaledImage.getHeight()) / 2;
      drawingImage.createGraphics().drawImage(scaledImage, xOffset, yOffset, null);
      markDirty();

    }
    sendPixels = true;
  }

  public void importDrawing(org.nlogo.api.File file)
      throws java.io.IOException {
    try {
      importDrawing(file.getInputStream());
    }
    catch (javax.imageio.IIOException ex) {
      throw new javax.imageio.IIOException("Unsupported image format: " + file.getPath(), ex);
    }
  }

  public void exportDrawingToCSV(java.io.PrintWriter writer) {
    if (!drawingBlank) {
      writer.println(Dump.csv().encode("DRAWING"));

      writer.println(Dump.csv().encode(Double.toString(world.patchSize())));

      String colorString = org.nlogo.util.HexString.toHexString(colors());

      Dump.csv().stringToCSV(writer, colorString);
    }
    writer.println();
  }

  public void setColors(int[] colors) {
    setUpDrawingImage();

    // rather than directly setting the values in the drawing buffer
    // make a temporary one and copy the image.  otherwise on OS 10.4
    // the drawing disappears when we try and draw to it again.
    // I expect has to do with the accessing the pixels directly
    // v. through the graphics object problem. ev 6/22/05

    java.awt.image.BufferedImage image =
        new java.awt.image.BufferedImage
            (width, height,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);

    image.setRGB(0, 0, width, height,
        colors, 0, width);

    java.awt.Graphics2D dg = drawingImage.createGraphics();

    dg.drawImage(image, 0, 0, null);

    drawingBlank = false;
    drawingDirty = true;
    sendPixels = true;
  }

  public void clearDrawing() {
    if (drawingImage != null) {
      setUpDrawingImage();
    }
  }

  public void stamp(org.nlogo.api.Agent agent, boolean erase) {
    Graphics2DWrapper tg = new Graphics2DWrapper(getAndCreateDrawing(false).createGraphics());
    tg.antiAliasing(true);
    tg.setComposite(erase ? java.awt.AlphaComposite.Clear : java.awt.AlphaComposite.SrcOver);

    topology.prepareToPaint(this, width, height);

    // we use world.patchSize here because the drawing does not
    // change resolution due to zooming. ev 4/2/08
    if (agent instanceof org.nlogo.api.Turtle) {
      turtleDrawer.drawTurtleShape
          (tg, topology, (org.nlogo.api.Turtle) agent, world.patchSize());
    } else if (agent instanceof org.nlogo.api.Link) {
      linkDrawer.drawLink
          (tg, topology, (org.nlogo.api.Link) agent, world.patchSize(), false);
    }
    tg.antiAliasing(false);

    markDirty();
  }

  public void drawLine(double x1, double y1, double x2, double y2,
                       Object penColor, double penSize, String penMode) {

    if (drawingImage == null) {
      setUpDrawingImage();
    }

    if (drawingImage != null) {
      Graphics2DWrapper tg = new Graphics2DWrapper((java.awt.Graphics2D) drawingImage.getGraphics());

      tg.setPenWidth(penSize);

      if (penMode.equals("erase")) {
        tg.setComposite(java.awt.AlphaComposite.Clear);

        drawWrappedLine(tg, x1, y1, x2, y2, penSize);

        tg.setComposite(java.awt.AlphaComposite.SrcOver);
      } else {
        tg.antiAliasing(true);

        tg.setColor(org.nlogo.api.Color.getColor(penColor));

        drawWrappedLine(tg, x1, y1, x2, y2, penSize);

        tg.antiAliasing(false);
      }
    }
  }

  private void drawWrappedLine(Graphics2DWrapper tg,
                               double x1, double y1, double x2, double y2,
                               double penSize) {
    double startX = x1;
    double startY = y1;
    double endX = x1;
    double endY = y1;
    double temp;
    if (endX < startX) {
      temp = endX;
      endX = startX;
      startX = temp;
    }
    if (endY < startY) {
      temp = endY;
      endY = startY;
      startY = temp;
    }

    double xdiff = x2 - x1;
    double ydiff = y2 - y1;
    double distX = x2 - x1;
    double distY = y2 - y1;
    double newStartX = 0;
    double newStartY = 0;
    double maxy = world.maxPycor() + 0.4999999;
    double maxx = world.maxPxcor() + 0.4999999;
    double miny = world.minPycor() - 0.5;
    double minx = world.minPxcor() - 0.5;
    double pixelSize = 1 / world.patchSize();

    do {
      endX = startX + distX;
      endY = startY + distY;

      if (endY < miny) {
        endX = (miny - startY) * xdiff / ydiff + startX;
        endY = miny;
        newStartY = maxy;
        newStartX = endX;
        if (newStartX == minx) {
          newStartX = maxx;
        } else if (newStartX == maxx) {
          newStartX = minx;
        }
      }
      if (endY > maxy) {
        endX = startX + ((maxy - startY) * xdiff / ydiff);
        endY = maxy;
        newStartX = endX;
        newStartY = miny;
        if (newStartX == minx) {
          newStartX = maxx;
        } else if (newStartX == maxx) {
          newStartX = minx;
        }
      }
      if (endX < minx) {
        endX = minx;
        endY = (ydiff * (endX - startX)) / xdiff + startY;
        newStartX = maxx;
        newStartY = endY;
        if (newStartY == miny) {
          newStartY = maxy;
        } else if (newStartY == maxy) {
          newStartY = miny;
        }
      }
      if (endX > maxx) {
        endX = maxx;
        endY = (ydiff * (endX - startX)) / xdiff + startY;
        newStartX = minx;
        newStartY = endY;
        if (newStartY == miny) {
          newStartY = maxy;
        } else if (newStartY == maxy) {
          newStartY = miny;
        }
      }

      topology.drawLine(tg, startX, startY, endX, endY, penSize);

      distX -= (endX - startX);
      distY -= (endY - startY);

      startX = newStartX;
      startY = newStartY;

    } while (StrictMath.abs(distY) >= pixelSize || StrictMath.abs(distX) >= pixelSize);

    markDirty();
  }

  // View settings
  public int fontSize() {
    throw new UnsupportedOperationException();
  }

  public double patchSize() {
    return world.patchSize();
  }

  public double viewWidth() {
    return world.worldWidth();
  }

  public double viewHeight() {
    return world.worldHeight();
  }

  public org.nlogo.api.Perspective perspective() {
    throw new UnsupportedOperationException();
  }

  public double viewOffsetX() {
    throw new UnsupportedOperationException();
  }

  public double viewOffsetY() {
    throw new UnsupportedOperationException();
  }

  public boolean drawSpotlight() {
    throw new UnsupportedOperationException();
  }

  public boolean renderPerspective() {
    return false;
  }

}
