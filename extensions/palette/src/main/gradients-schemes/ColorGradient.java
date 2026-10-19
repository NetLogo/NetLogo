import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ColorGradient extends BufferedImage {

  int[][] GradientRGBArray;

  // create a rectangle of 1 by 256 pixels with all the gradients.
  public ColorGradient (Color startColor, Color endColor, int width) {
    super (width, 1, BufferedImage.TYPE_INT_RGB);
    GradientPaint gradientPaint = new GradientPaint(0, 0, startColor,
                            width, 0, endColor ,
                            false);
    Graphics2D g = createGraphics();
    g.setPaint(gradientPaint);
    g.fillRect(0, 0, width, 1);
  }

  // returns an array with 3 arrays with the separate rgb channels.
  public int [] getPixelRGBArray(int x , int y) {
    int rgb = getRGB(x,y);
    Color c = new Color(rgb);
    int[] pixelRGBArray = {c.getRed(), c.getGreen(), c.getBlue()};
    return pixelRGBArray;
  }

  // returns an array with arrays of rgb colors in the gradient
  public int [][] getGradientRGBArray() {
    // Get all the pixels
    final int w = getWidth();
    final int h = getHeight();
    final int[] gradientRGB = new int[w*h];
    getRGB(0, 0, w, h, gradientRGB, 0, w);

    GradientRGBArray = new int[gradientRGB.length][3];
    Color c;
    for (int i=0; i < gradientRGB.length; i++) {
      c = new Color(gradientRGB[i]);
      GradientRGBArray[i][0] = c.getRed();
      GradientRGBArray[i][1] = c.getGreen();
      GradientRGBArray[i][2] = c.getBlue();
    }
    return GradientRGBArray;
  }
}
