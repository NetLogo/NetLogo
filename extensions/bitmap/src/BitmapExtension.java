package org.nlogo.extensions.bitmap;

import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.PrimitiveManager;
import org.nlogo.api.Syntax;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.Argument;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.ExtensionContext;
import org.nlogo.nvm.FileManager;
import org.nlogo.nvm.Workspace;

import java.awt.image.BufferedImage;

public class BitmapExtension extends DefaultClassManager {

  public void load(PrimitiveManager primitiveManager) {
    primitiveManager.addPrimitive("import", new LoadImage());
    // saves to disk as PNG format
    primitiveManager.addPrimitive("export", new SaveImage());
    // returns a LogoBitmap image from NetLogo's primary view, as if by export-view
    primitiveManager.addPrimitive("from-view", new GrabView());

    primitiveManager.addPrimitive("scaled", new Scale());
    //computes the absolute value of the pixel-wise RGB difference between two images.
    primitiveManager.addPrimitive("difference-rgb", new DifferenceRGB());
    //returns an image
    primitiveManager.addPrimitive("channel", new ExtractChannel());
    primitiveManager.addPrimitive("to-grayscale", new Grayscale());

    primitiveManager.addPrimitive("copy-to-drawing", new ImportToDrawing());
    primitiveManager.addPrimitive("copy-to-pcolors", new ImportToPcolors());
    // maybe we should give access to other properties
    // this seems fine for now, easy enough to add more later.
    primitiveManager.addPrimitive("width", new Width());
    primitiveManager.addPrimitive("height", new Height());
    // returns a 3-element list describing the amount of R, G, and B
    // in the image, by summing across all pixels, and normalizing each
    // component by the number of pixels in the image, so each component
    // ranges from 0 to 255.
    primitiveManager.addPrimitive("average-color", new RGBLevels());
  }

  private static BufferedImage getBitmapFromArgument(Argument arg)
      throws ExtensionException, LogoException {
    Object obj = arg.get();
    if (!(obj instanceof BufferedImage)) {
      throw new org.nlogo.api.ExtensionException("not a bitmap: "
          + org.nlogo.api.Dump.logoObject(obj));
    }
    return (BufferedImage) obj;
  }

  public static class LoadImage extends DefaultReporter {

    public Syntax getSyntax() {
      return Syntax.reporterSyntax(new int[]{Syntax.TYPE_STRING},
          Syntax.TYPE_WILDCARD);
    }

    public String getAgentClassString() {
      return "O";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      FileManager fm = ((ExtensionContext) context).workspace().fileManager();
      try {
        String path = fm.attachPrefix(args[0].getString());
        return new LogoBitmap(
            javax.imageio.ImageIO.read(fm.getFile(path).getInputStream()));
      } catch (java.io.IOException e) {
        throw new ExtensionException(e.getMessage());
      }
    }
  }

  public static class SaveImage extends DefaultCommand {

    public Syntax getSyntax() {
      int[] right = {Syntax.TYPE_WILDCARD, Syntax.TYPE_STRING};
      return Syntax.commandSyntax(right);
    }

    public String getAgentClassString() {
      return "O";
    }

    public void perform(Argument args[], Context context)
        throws ExtensionException, LogoException {
      BufferedImage image = getBitmapFromArgument(args[0]);

      try {
        String filename = context.attachCurrentDirectory(args[1].getString());
        java.io.FileOutputStream stream =
            new java.io.FileOutputStream(filename);
        javax.imageio.ImageIO.write(image, "png", stream);
        stream.close();
      } catch (java.io.IOException e) {
        throw new ExtensionException(e.getMessage());
      }
    }
  }

  public static class GrabView extends DefaultReporter {

    public Syntax getSyntax() {
      return Syntax.reporterSyntax(new int[]{}, Syntax.TYPE_WILDCARD);
    }

    public String getAgentClassString() {
      return "O";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      ExtensionContext ec = (ExtensionContext) context;
      Workspace ws = ec.workspace();

      return new LogoBitmap(ws.exportView());
    }
  }

  public static class Width extends DefaultReporter {

    public Syntax getSyntax() {
      return Syntax.reporterSyntax(new int[]{Syntax.TYPE_WILDCARD},
          Syntax.TYPE_NUMBER);
    }

    public String getAgentClassString() {
      return "O";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      return Double.valueOf(getBitmapFromArgument(args[0]).getWidth());
    }
  }

  public static class Height extends DefaultReporter {

    public Syntax getSyntax() {
      return Syntax.reporterSyntax(new int[]{Syntax.TYPE_WILDCARD},
          Syntax.TYPE_NUMBER);
    }

    public String getAgentClassString() {
      return "O";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      return Double.valueOf(getBitmapFromArgument(args[0]).getHeight());
    }
  }

  public static class Scale extends DefaultReporter {

    public Syntax getSyntax() {
      return Syntax.reporterSyntax(new int[]{Syntax.TYPE_WILDCARD,
          Syntax.TYPE_NUMBER, Syntax.TYPE_NUMBER},
          Syntax.TYPE_WILDCARD);
    }

    public String getAgentClassString() {
      return "O";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      BufferedImage image = getBitmapFromArgument(args[0]);

      float scalex = (float) args[1].getDoubleValue()
          / (float) image.getWidth();
      float scaley = (float) args[2].getDoubleValue()
          / (float) image.getHeight();

      return scale(image, scalex, scaley);
    }
  }

  private static LogoBitmap scale(BufferedImage image, float scalex,
                                  float scaley) {
    return new LogoBitmap(new java.awt.image.AffineTransformOp(
        java.awt.geom.AffineTransform
            .getScaleInstance(scalex, scaley),
        java.awt.image.AffineTransformOp.TYPE_BILINEAR).filter(image,
        null));
  }

  public static class ImportToDrawing extends DefaultCommand {

    public Syntax getSyntax() {
      int[] right = {Syntax.TYPE_WILDCARD, Syntax.TYPE_NUMBER,
          Syntax.TYPE_NUMBER};
      return Syntax.commandSyntax(right);
    }

    public String getAgentClassString() {
      return "O";
    }

    public void perform(Argument args[], Context context)
        throws ExtensionException, LogoException {
      BufferedImage image = getBitmapFromArgument(args[0]);

      int xOffset = args[1].getIntValue();
      int yOffset = args[2].getIntValue();

      java.awt.image.BufferedImage drawing = context.getDrawing();

      drawing.createGraphics().drawImage(image, xOffset, yOffset,
          null);
    }
  }

  public static class ImportToPcolors extends DefaultCommand {

    public Syntax getSyntax() {
      int[] right = {Syntax.TYPE_WILDCARD, Syntax.TYPE_BOOLEAN};
      return Syntax.commandSyntax(right);
    }

    public String getAgentClassString() {
      return "O";
    }

    public void perform(Argument args[], Context context)
        throws ExtensionException, LogoException {
      BufferedImage image = getBitmapFromArgument(args[0]);

      context.importPcolors(image, args[1].getBooleanValue());
    }
  }


  public static class DifferenceRGB extends DefaultReporter {

    public Syntax getSyntax() {
      return Syntax.reporterSyntax(new int[]{Syntax.TYPE_WILDCARD,
          Syntax.TYPE_WILDCARD},
          Syntax.TYPE_WILDCARD);
    }

    public String getAgentClassString() {
      return "O";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      BufferedImage image1 = getBitmapFromArgument(args[0]);
      BufferedImage image2 = getBitmapFromArgument(args[1]);

      if ((image1.getWidth() != image2.getWidth()) || (image1.getHeight() != image2.getHeight())) {
        throw new org.nlogo.api.ExtensionException("The two images you are 'differencing' must be the same dimensions!");
      }
      return diff(image1, image2);
    }
  }

  /**
   * The two images passed to this method MUST be the same width & height.
   */
  private static LogoBitmap diff(BufferedImage src1, BufferedImage src2) {

    int width = src1.getWidth();
    int height = src1.getHeight();
    int[] rgbArray1 = new int[width * height];
    int[] rgbArray2 = new int[width * height];
    src1.getRGB(0, 0, width, height, rgbArray1, 0, width);
    src2.getRGB(0, 0, width, height, rgbArray2, 0, width);

    for (int i = 0; i < rgbArray1.length; i++) {
      // We use bitwise XOR to get the absolute value of
      // the difference between each of the R,G,B components.
      // However, we want full opacity, so we set the ALPHA bits
      // using "| 0xFF000000".
      rgbArray1[i] = (rgbArray1[i] ^ rgbArray2[i]) | 0xFF000000;
    }
    java.awt.image.WritableRaster raster = src1.copyData(null);
    BufferedImage dest = new BufferedImage(src1.getColorModel(), raster, src1.isAlphaPremultiplied(), null);
    dest.setRGB(0, 0, width, height, rgbArray1, 0, width);
    return new LogoBitmap(dest);
  }


  public static class ExtractChannel extends DefaultReporter {

    public Syntax getSyntax() {
      return Syntax.reporterSyntax(new int[]{Syntax.TYPE_WILDCARD,
          Syntax.TYPE_NUMBER},
          Syntax.TYPE_WILDCARD);
    }

    public String getAgentClassString() {
      return "O";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      BufferedImage image = getBitmapFromArgument(args[0]);
      int channel = args[1].getIntValue();
      if (channel < 0 || channel > 3) {
        throw new org.nlogo.api.ExtensionException("The channel # must be between 0 and 3 (0=alpha,1=red,2=green,3=blue).");
      }
      return extractChannel(image, channel);
    }
  }

  /**
   * @param img
   * @param channel 0 = alpha, 1 = red, 2 = green, 3 = blue
   * @return a grayscale image representing the value of the specified channel at each point.
   */
  private static LogoBitmap extractChannel(BufferedImage img, int channel) {
    int width = img.getWidth();
    int height = img.getHeight();
    int[] rgbArray = new int[width * height];
    img.getRGB(0, 0, width, height, rgbArray, 0, width);

    for (int i = 0; i < rgbArray.length; i++) {
      int val = (rgbArray[i] & (0xFF << (8 * (3 - channel)))) >> (8 * (3 - channel));
      rgbArray[i] = val | (val << 8) | (val << 16) | 0xFF000000;
    }
    java.awt.image.WritableRaster raster = img.copyData(null);
    BufferedImage dest = new BufferedImage(img.getColorModel(), raster, img.isAlphaPremultiplied(), null);
    dest.setRGB(0, 0, width, height, rgbArray, 0, width);
    return new LogoBitmap(dest);
  }

  public static class Grayscale extends DefaultReporter {

    public Syntax getSyntax() {
      return Syntax.reporterSyntax(new int[]{Syntax.TYPE_WILDCARD},
          Syntax.TYPE_WILDCARD);
    }

    public String getAgentClassString() {
      return "O";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      BufferedImage colorImage = getBitmapFromArgument(args[0]);
      BufferedImage grayImage = new BufferedImage(colorImage.getWidth(), colorImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
      java.awt.Graphics g = grayImage.getGraphics();
      g.drawImage(colorImage, 0, 0, null);
      g.dispose();
      return grayImage;
    }
  }

  public static class RGBLevels extends DefaultReporter {

    public Syntax getSyntax() {
      return Syntax.reporterSyntax(new int[]{Syntax.TYPE_WILDCARD},
          Syntax.TYPE_LIST);
    }

    public String getAgentClassString() {
      return "O";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {
      BufferedImage image = getBitmapFromArgument(args[0]);

      int width = image.getWidth();
      int height = image.getHeight();
      int[] rgbArray = new int[width * height];
      image.getRGB(0, 0, width, height, rgbArray, 0, width);
      long rSum = 0;
      long gSum = 0;
      long bSum = 0;
      for (int i = 0; i < rgbArray.length; i++) {
        rSum += (rgbArray[i] >> 16 & 0xff);
        gSum += (rgbArray[i] >> 8 & 0xff);
        bSum += rgbArray[i] & 0xff;
      }
      LogoListBuilder lst = new LogoListBuilder();
      lst.add((double) rSum / width / height);
      lst.add((double) gSum / width / height);
      lst.add((double) bSum / width / height);
      return lst.toLogoList();
    }
  }

}
