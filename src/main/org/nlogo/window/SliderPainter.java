// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

public abstract strictfp class SliderPainter {
  abstract public java.awt.Dimension getMinimumSize();

  abstract public java.awt.Dimension getPreferredSize(java.awt.Font font);

  abstract public java.awt.Dimension getMaximumSize();

  abstract public void doLayout();

  abstract public void paintComponent(java.awt.Graphics g);

  abstract public void dettach();
}
