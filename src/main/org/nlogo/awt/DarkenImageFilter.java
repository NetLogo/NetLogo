package org.nlogo.awt;

public strictfp class DarkenImageFilter
    extends java.awt.image.RGBImageFilter {

  private static final int OPAQUE_ALPHA = 0xff000000;

  private final double level;

  public DarkenImageFilter(double level) {
    this.level = level;
    canFilterIndexColorModel = true;
  }

  @Override
  public int filterRGB(int x, int y, int rgb) {
    int red = (rgb >> 16) & 0xff;
    int green = (rgb >> 8) & 0xff;
    int blue = (rgb) & 0xff;
    red = (int) ((1.0 - level) * red);
    green = (int) ((1.0 - level) * green);
    blue = (int) ((1.0 - level) * blue);
    int newRGB = rgb & OPAQUE_ALPHA;
    newRGB = newRGB | (red << 16);
    newRGB = newRGB | (green << 8);
    return newRGB | (blue);
  }

}
