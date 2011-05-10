package org.nlogo.extensions.bitmap;

import java.awt.image.*;

public class LogoBitmap
    extends BufferedImage
    implements org.nlogo.api.ExtensionObject {
  public LogoBitmap(BufferedImage image) {
    super(image.getColorModel(), image.getRaster(), image.isAlphaPremultiplied(), null);
  }

  public String dump(boolean readable, boolean exporting, boolean reference) {
    // we need to do something more complicated for exporting, obviously
    return getWidth() + "x" + getHeight();
  }

  public String getExtensionName() {
    return "bitmap";
  }

  public String getNLTypeName() {
    return "";
  }

  public boolean recursivelyEqual(Object o) {
    // I guess we could do something more complicated like
    // comparing the data, but why?
    return o == this;
  }
}

