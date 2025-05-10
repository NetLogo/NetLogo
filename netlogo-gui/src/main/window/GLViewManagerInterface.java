// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

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

  void editFinished();

  void close();

  void addCustomShapes(String filename)
      throws java.io.IOException,
      org.nlogo.shape.InvalidShapeDescriptionException;
}
