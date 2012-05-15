// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

// implemented by InterfacePanel and InterfacePanelLite - ST 10/14/03

public interface WidgetContainer {
  String getBoundsString(Widget widget);

  java.awt.Rectangle getUnzoomedBounds(java.awt.Component component);

  void resetZoomInfo(Widget widget);

  void resetSizeInfo(Widget widget);

  boolean isZoomed();

  Widget loadWidget(String[] strings, String modelVersion);
}
