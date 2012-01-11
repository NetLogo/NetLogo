// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.api.I18N;
import org.nlogo.awt.RowLayout;

public strictfp class ViewControlStrip
    extends javax.swing.JPanel {

  private final GUIWorkspace workspace;
  private final ViewWidget viewWidget;

  private static final int MIN_HEIGHT = 20;
  // this is needed because we don't want the tooltips to overlap
  // the graphics window itself, because that results in expensive
  // repaints - ST 10/27/03
  private static final java.awt.Point TOOL_TIP_OFFSET =
      new java.awt.Point(0, -18);

  /// setup and layout

  ViewControlStrip(GUIWorkspace workspace, ViewWidget viewWidget) {
    this.workspace = workspace;
    this.viewWidget = viewWidget;
    setBackground(InterfaceColors.GRAPHICS_BACKGROUND);
    java.awt.BorderLayout layout = new java.awt.BorderLayout();
    layout.setVgap(0);
    setLayout(layout);
    if (workspace.kioskLevel == GUIWorkspace.KioskLevel.NONE) {
      add(viewWidget.tickCounter, java.awt.BorderLayout.CENTER);
    } else {
      add(viewWidget.tickCounter, java.awt.BorderLayout.WEST);
      SpeedSliderPanel speedSlider = new SpeedSliderPanel(workspace, false);
      speedSlider.setOpaque(false);
      add(speedSlider, java.awt.BorderLayout.CENTER);
    }
  }

  @Override
  public java.awt.Dimension getMinimumSize() {
    return new java.awt.Dimension
        (super.getMinimumSize().width,
            MIN_HEIGHT);
  }

  // special case: on every platform, we insist that the preferred size
  // be exactly the same, so the pixel-exact same size gets saved in the
  // model on every platform - ST 9/21/03
  @Override
  public java.awt.Dimension getPreferredSize() {
    if (viewWidget.isZoomed()) {
      return super.getPreferredSize();
    } else {
      return new java.awt.Dimension
          (super.getPreferredSize().width,
              MIN_HEIGHT);
    }
  }

  /// misc

  void reset() {
    // this next line shouldn't be necessary, but in Java 1.4.2U1DP3
    // on OS X it became necessary in the applet, which is
    // probably a VM bug, but it's OK, I think it's harmless
    // - ST 7/13/04
    doLayout();
  }

}
