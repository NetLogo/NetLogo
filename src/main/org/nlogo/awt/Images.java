package org.nlogo.awt;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

public strictfp class Images {

  // this class is not instantiable
  private Images() { throw new IllegalStateException(); }

  public static boolean loadImage(Image image) {
    MediaTracker mt =
        new MediaTracker(new Component() {
        });
    mt.addImage(image, 0);
    try {
      mt.waitForAll();
    } catch (InterruptedException ex) {
      return false;
    }
    return !mt.isErrorAny();
  }

  public static Image loadImageResource(String path) {
    Image image =
        Toolkit.getDefaultToolkit().getImage
            (Utils.class.getResource(path));
    return loadImage(image)
        ? image
        : null;
  }

  public static Image loadImageFile(String path, boolean cache) {
    Image image;
    if (cache) {
      image = Toolkit.getDefaultToolkit().getImage(path);
    } else {
      image = Toolkit.getDefaultToolkit().createImage(path);
    }
    return loadImage(image)
        ? image
        : null;
  }

  public static BufferedImage paintToImage(Component comp) {
    BufferedImage image =
        new BufferedImage
            (comp.getWidth(), comp.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
    // If we just call paint() here we get weird results on
    // windows printAll appears to work ev 5/13/09
    comp.printAll(image.getGraphics());
    return image;
  }

}
