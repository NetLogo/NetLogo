// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

public interface WidgetWrapperInterface {
  java.awt.Dimension getSize();

  java.awt.Dimension getPreferredSize();

  java.awt.Dimension getMaximumSize();

  void setSize(int width, int height);

  void setSize(java.awt.Dimension size);

  boolean verticallyResizable();

  void widgetChanged();

  int gridSnap();

  boolean isNew();

  Widget widget();
}
