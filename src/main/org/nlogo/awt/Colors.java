package org.nlogo.awt;

import java.awt.Color;

public strictfp class Colors {

  // this class is not instantiable
  private Colors() { throw new IllegalStateException(); }

  /**
   * Mixes the rgb components of two colors.
   *
   * @param mix the proportion, from 0 to 1, of the first color in the mix.
   * @return a new color with <code>red = mix*(c1.red) + (1-mix)*c2.red</code>, etc.
   */
  public static Color mixColors(Color c1, Color c2, double mix) {
    mix = StrictMath.min(mix, 1);
    mix = StrictMath.max(mix, 0);
    return
        new Color((int) ((c1.getRed() * mix) + (c2.getRed() * (1 - mix))),
            (int) ((c1.getGreen() * mix) + (c2.getGreen() * (1 - mix))),
            (int) ((c1.getBlue() * mix) + (c2.getBlue() * (1 - mix))));
  }

  /**
   * Converts a java.awt.Color to a 6-digit hex string suitible for HTML/CSS tags. *
   */
  public static String AWTColorToHex(Color c) {
    String s = Integer.toHexString(c.getRGB());
    s = s.substring(s.length() - 6);
    return s;
  }

  /**
   * Wraps a string with HTML font tag for color. *
   */
  public static String colorize(String s, Color c) {
    String str =
        "<font color=\""
            + "#"
            + AWTColorToHex(c)
            + "\">"
            + s
            + "</font>";
    return str;
  }

}
