// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

public final strictfp class Utils {

  // this class is not instantiable
  private Utils() { throw new IllegalStateException(); }

  public static void drawStringInBox(Graphics g, String string, int x, int y) {
    Color color = g.getColor();
    FontMetrics metrics = g.getFontMetrics();
    y -= metrics.getAscent();
    int width = metrics.stringWidth(string);
    int height = metrics.getHeight();
    g.setColor(Color.WHITE);
    g.fillRect(x, y, width + 6, height + 6);
    g.setColor(color);
    g.drawRect(x, y, width + 6, height + 6);
    g.drawString(string, x + 3, y + 3 + metrics.getAscent());
  }

}
