// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import java.awt.Color;

import org.nlogo.theme.ThemeSync;

public interface GLViewManagerInterface
    extends LocalViewInterface, ThemeSync {
  void open()
      throws JOGLLoadingException;

  boolean isFullscreen();

  boolean displayOn();

  void displayOn(boolean displayOn);

  void antiAliasingOn(boolean on);

  boolean antiAliasingOn();

  void setWireframeOn(boolean on);

  boolean wireframeOn();

  Color getBgColor();

  void setBgColor(Color color);

  Color getWireframeColor();

  void setWireframeColor(Color color);

  void editFinished();

  void close();

  void addCustomShapes(String filename)
      throws java.io.IOException,
      org.nlogo.shape.InvalidShapeDescriptionException;
}
