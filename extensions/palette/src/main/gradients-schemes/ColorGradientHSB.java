import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

import org.nlogo.core.LogoList;

public class ColorGradientHSB {

  double[][] GradientHSBArray;

  // create a rectangle of 1 by 256 pixels with all the gradients.
  public void genArray(double[] startColor, double[] endColor, int width) {
    double[] inc = new double[3]; width--;
    if (endColor[0] > startColor[0]) {
      if (startColor[0] + 360 - endColor[0] < endColor[0] - startColor[0]) {
        inc[0] = (endColor[0] - (startColor[0] + 360.0)) / width;
      }
      else { inc[0] = (endColor[0] - startColor[0]) / width; }
    }
    else {
      if (endColor[0] + 360.0 - startColor[0] < startColor[0] - endColor[0]) {
        inc[0] = (endColor[0] + 360.0 - startColor[0]) / width;
      }
      else { inc[0] = (endColor[0] - startColor[0]) / width; }
    }
    inc[1] = (endColor[1] - startColor[1]) / width;
    inc[2] = (endColor[2] - startColor[2]) / width;
    width++;

    GradientHSBArray = new double[width][3];
    GradientHSBArray[0] = startColor;
    for (int i = 1; i < width; i++) {
      for (int j = 0; j < 3; j++) {
        GradientHSBArray[i][j] = GradientHSBArray[i-1][j] + inc[j];
        if (j > 0) {
          GradientHSBArray[i][j] = Math.min(100, Math.max(0, GradientHSBArray[i][j]));
        }
        else {
          if (GradientHSBArray[i][j] >= 360) {GradientHSBArray[i][j] -= 360; }
          if (GradientHSBArray[i][j] < 0) {GradientHSBArray[i][j] += 360; }
        }
      }
    }
  }

  public double[][] getHSBArray() {
    return GradientHSBArray;
  }
}
