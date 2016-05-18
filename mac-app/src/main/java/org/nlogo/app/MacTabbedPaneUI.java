// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app;

import java.awt.Graphics;

import com.apple.laf.AquaTabbedPaneContrastUI;

// Gets rid of the weird top border that the default UI on mac contains
class MacTabbedPaneUI extends AquaTabbedPaneContrastUI {
  protected void paintContentBorder(final Graphics g, final int tabPlacement, final int selectedIndex) {
  }
}
