// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

public interface MenuBarFactory {
  javax.swing.JMenu createFileMenu();

  javax.swing.JMenu createEditMenu();

  javax.swing.JMenu createToolsMenu();

  javax.swing.JMenu createZoomMenu();

  javax.swing.JMenu addHelpMenu(javax.swing.JMenuBar menuBar);
}
