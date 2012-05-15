// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

public interface LocalViewInterface
    extends org.nlogo.api.ViewInterface {
  java.awt.Component getExportWindowFrame();

  java.awt.image.BufferedImage exportView();

  void displaySwitch(boolean on);

  boolean displaySwitch();
}
