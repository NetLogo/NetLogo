package org.nlogo.awt;

import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

public strictfp class Fonts {

  // this class is not instantiable
  private Fonts() { throw new IllegalStateException(); }

  public static String platformFont() {
    if (System.getProperty("os.name").startsWith("Mac")) {
      return "Lucida Grande";
    } else {
      return "Sans-serif";
    }
  }

  public static String platformMonospacedFont() {
    if (System.getProperty("os.name").startsWith("Mac")) {
      return "Monaco";
    } else if (System.getProperty("os.name").startsWith("Windows")) {
      String[] fonts =
          GraphicsEnvironment.getLocalGraphicsEnvironment()
              .getAvailableFontFamilyNames();
      for (int i = 0; i < fonts.length; i++) {
        if (fonts[i].equalsIgnoreCase("Lucida Console")) {
          return fonts[i];
        }
      }
      return "Monospaced";
    } else {
      return "Monospaced";
    }
  }

  public static void adjustDefaultFont(Component comp) {
    if (System.getProperty("os.name").startsWith("Mac")) {
      comp.setFont
          (new Font
              (platformFont(), Font.PLAIN, 11));
    } else if (!(System.getProperty("os.name").startsWith("Windows"))) {
      comp.setFont
          (new Font
              (platformFont(), Font.PLAIN, 12));
    }
  }

  public static void adjustDefaultMonospacedFont(Component comp) {
    if (System.getProperty("os.name").startsWith("Mac")) {
      comp.setFont
          (new Font
              (platformMonospacedFont(), Font.PLAIN, 12));
    }
  }

}
