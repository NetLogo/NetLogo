// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentException;
import org.nlogo.api.I18N;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.util.HashMap;
import java.util.Map;

public strictfp class ImportPatchColors {
  public static void importPatchColors(org.nlogo.api.File imageFile, World world,
                                       boolean asNetLogoColors)
      throws java.io.IOException {

    String fileName = imageFile.getAbsolutePath();
    BufferedImage image = ImageIO.read(imageFile.getInputStream());
    // sometime we have to throw the exception ourselves,
    // because we just get back null. Booo.  -CLB
    if (image == null) {
      throw new java.io.IOException(I18N.errorsJ().getN(
          "org.nlogo.agent.ImportPatchColors.unsupportedImageFormat", fileName));
    }

    doImport(image, world, asNetLogoColors);
  }

  public static void doImport(BufferedImage image, World world, boolean asNetLogoColors) {
    float scalex = (float) (world.worldWidth()) / (float) image.getWidth();
    float scaley = (float) (world.worldHeight()) / (float) image.getHeight();
    float scale = scalex < scaley ? scalex : scaley;

    java.awt.image.BufferedImage scaledImage = null;

    if (scale != 1) {

      AffineTransformOp trans =
          new AffineTransformOp(AffineTransform.getScaleInstance(scale, scale),
              AffineTransformOp.TYPE_BILINEAR);
      // To workaround a java bug, if our image was read
      // into a grayscale color space BufferedImage, than we
      // want to make sure we scale to the same color model
      // so that the colors don't get brightened.  However,
      // we can't do this for image buffers with alpha
      // values, or the scaling gets hosed too.  A curse
      // upon all "open source" languages with closed source
      // implementations. -- CLB
      if (image.getColorModel().getColorSpace().getType()
          == java.awt.color.ColorSpace.TYPE_GRAY
          && !image.getColorModel().hasAlpha()) {
        scaledImage =
            trans.createCompatibleDestImage(image,
                image.getColorModel());
        trans.filter(image, scaledImage);
      } else {
        scaledImage = trans.filter(image, null);
      }

    } else {
      scaledImage = image;
    }

    SampleModel sm = scaledImage.getSampleModel();
    // Because we can end up with a scaled image that is one
    // pixel too large when the AffineTransform does some sort
    // of rounding, we need to make sure we use the minimum in
    // each dimensions between the scaled image and the number
    // of patches.  A 300x300 image imported into a world with
    // 63x63 patches is an example that hits this sore spot.
    // -- CLB 09/09/05
    int maxwidth = StrictMath.min(scaledImage.getWidth(), world.worldWidth());
    int maxheight = StrictMath.min(scaledImage.getHeight(), world.worldHeight());

    DataBuffer db = scaledImage.getData().getDataBuffer();
    ColorModel cm = scaledImage.getColorModel();
    CachedColorLookup lookup = new CachedColorLookup();

    // We want to "center" in the dimension that does not scale to the edge of the world
    int woff = (int) StrictMath.floor((world.worldWidth() - scaledImage.getWidth()) / 2);
    int hoff = (int) StrictMath.floor((world.worldHeight() - scaledImage.getHeight()) / 2);

    int currentPxcor = world.minPxcor() + woff;
    for (int i = 0; i < maxwidth; i++) {
      int currentPycor = world.maxPycor() - hoff;
      for (int j = 0; j < maxheight; j++) {
        Object transferType = sm.getDataElements(i, j, null, db);
        // do not import for colors which have a 0 alpha value
        // as they should be transparent -- 5/7/05 CLB
        if (cm.getAlpha(transferType) != 0) {
          int argb = cm.getRGB(transferType);
          if (asNetLogoColors) {
            world.fastGetPatchAt
                (currentPxcor, currentPycor)
                .pcolor(lookup.lookupColor(argb));
          } else {
            try {
              world.fastGetPatchAt
                  (currentPxcor, currentPycor).pcolor
                  (org.nlogo.api.Color.getRGBListByARGB(argb));
            } catch (AgentException e) {
              // should be impossible since we know we are passing
              // pcolor() a good list - ST 3/25/08
              throw new IllegalStateException(e);
            }
          }
        }
        currentPycor--;
      }
      currentPxcor++;
    }
  }

  private static class CachedColorLookup {
    private final Map<Integer, Double> cache =
        new HashMap<Integer, Double>();

    double lookupColor(int argb) {
      Integer boxed = Integer.valueOf(argb);
      Double value = cache.get(boxed);

      if (value == null && !cache.containsKey(boxed)) {
        value = Double.valueOf(org.nlogo.api.Color.getClosestColorNumberByARGB(argb));
        cache.put(boxed, value);
      }
      return value.doubleValue();
    }
  }
}
