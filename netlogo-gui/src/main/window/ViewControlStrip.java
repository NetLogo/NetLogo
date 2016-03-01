// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.core.I18N;
import org.nlogo.awt.RowLayout;

public strictfp class ViewControlStrip
    extends javax.swing.JPanel {

  private final GUIWorkspace workspace;
  private final ViewWidget viewWidget;
  private final SizeControl sizeControlXY;
  private final SizeControl sizeControlX;
  private final SizeControl sizeControlY;

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
      javax.swing.JPanel sizeControlPanel = new javax.swing.JPanel();
      sizeControlPanel.setLayout
          (new RowLayout(1, java.awt.Component.RIGHT_ALIGNMENT,
              java.awt.Component.CENTER_ALIGNMENT));

      sizeControlXY = new SizeControl("/images/arrowsdiag.gif", 1, 1);
      sizeControlXY.setToolTipText("Change width and height of world");
      sizeControlPanel.add(sizeControlXY);

      sizeControlX = new SizeControl("/images/arrowsx.gif", 1, 0);
      sizeControlX.setToolTipText("Change width of world");
      sizeControlPanel.add(sizeControlX);

      sizeControlY = new SizeControl("/images/arrowsy.gif", 0, 1);
      sizeControlY.setToolTipText("Change height of world");
      sizeControlPanel.add(sizeControlY);

      add(sizeControlPanel, java.awt.BorderLayout.WEST);
      sizeControlPanel.setOpaque(false);
      add(viewWidget.tickCounter, java.awt.BorderLayout.CENTER);
    } else {
      sizeControlXY = null;
      sizeControlX = null;
      sizeControlY = null;
      add(viewWidget.tickCounter, java.awt.BorderLayout.WEST);
      SpeedSliderPanel speedSlider = new SpeedSliderPanel(workspace, false);
      speedSlider.setOpaque(false);
      add(speedSlider, java.awt.BorderLayout.CENTER);
    }
    if (workspace.kioskLevel == GUIWorkspace.KioskLevel.NONE) {
      ThreedButton threedButton = new ThreedButton();
      add(threedButton, java.awt.BorderLayout.EAST);
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

  @Override
  public void doLayout() {
    if (workspace.kioskLevel == GUIWorkspace.KioskLevel.NONE) {
      sizeControlXY.setVisible(true);
      sizeControlX.setVisible(true);
      sizeControlY.setVisible(true);
    }
    super.doLayout();
  }

  /// misc

  void reset() {
    enableSizeControls
        ((workspace.world.maxPxcor() == -workspace.world.minPxcor()
            || workspace.world.minPxcor() == 0 || workspace.world.maxPxcor() == 0),
            (workspace.world.maxPycor() == -workspace.world.minPycor()
                || workspace.world.minPycor() == 0 || workspace.world.maxPycor() == 0));
    // this next line shouldn't be necessary, but in Java 1.4.2U1DP3
    // on OS X it became necessary in the applet, which is
    // probably a VM bug, but it's OK, I think it's harmless
    // - ST 7/13/04
    doLayout();
  }

  void enableSizeControls(boolean x, boolean y) {
    if (workspace.kioskLevel == GUIWorkspace.KioskLevel.NONE) {
      sizeControlX.setEnabled(x);
      sizeControlY.setEnabled(y);
      sizeControlXY.setEnabled(x && y);
    }
  }

  /// subparts

  private class ThreedButton
      extends javax.swing.JButton {
    public ThreedButton() {
      super(" 3D "); // spaces so it isn't so tiny
      setFont
          (new java.awt.Font(org.nlogo.awt.Fonts.platformFont(),
              java.awt.Font.PLAIN, 10));
      setBackground(InterfaceColors.GRAPHICS_BACKGROUND);
      setBorder(org.nlogo.swing.Utils.createWidgetBorder());
      setFocusable(false);
      setToolTipText("Switch to 3D view");
      addActionListener(workspace.switchTo3DViewAction);
    }

    @Override
    public java.awt.Point getToolTipLocation(java.awt.event.MouseEvent e) {
      return TOOL_TIP_OFFSET;
    }

    @Override
    public void updateUI() {
      // without this it looks funny on Windows - ST 9/18/03
      setUI(new javax.swing.plaf.basic.BasicButtonUI());
    }
  }

  private class SizeControl
      extends javax.swing.JButton
      implements java.awt.event.ActionListener {
    private final int sexChange; // how much this instance alters screen-edge-x
    private final int seyChange; //  "   "    "      "       "    screen-egde-y
    private java.awt.Point mousePressLoc;

    SizeControl(String imagePath, int sexChange, int seyChange) {
      this.sexChange = sexChange;
      this.seyChange = seyChange;
      setOpaque(false);
      setFocusable(false);

      setBorder
          (javax.swing.BorderFactory.createEmptyBorder(0, 2, 0, 2));
      setIcon(new javax.swing.ImageIcon
          (SizeControl.class.getResource
              (imagePath)));
      addMouseListener
          (new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
              mousePressLoc = e.getPoint();
            }
          });
      addActionListener(this);
    }

    @Override
    public java.awt.Point getToolTipLocation(java.awt.event.MouseEvent e) {
      return TOOL_TIP_OFFSET;
    }

    @Override
    public void updateUI() {
      // without this it looks funny on Windows - ST 9/18/03
      setUI(new javax.swing.plaf.basic.BasicButtonUI());
    }

    private boolean increase() {
      if (sexChange == 1 && seyChange == 0) {
        return mousePressLoc.x >= getWidth() / 2;
      } else if (sexChange == 0 && seyChange == 1) {
        return mousePressLoc.y >= getHeight() / 2;
      } else {
        return mousePressLoc.x + mousePressLoc.y
            >= (getHeight() + getWidth()) / 2;
      }
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      if (!checkWithUser()) {
        return;
      }

      int maxPxcor = workspace.world.maxPxcor();
      int minPxcor = workspace.world.minPxcor();
      int maxPycor = workspace.world.maxPycor();
      int minPycor = workspace.world.minPycor();

      int deltax = sexChange * (increase() ? 1 : -1);
      int deltay = seyChange * (increase() ? 1 : -1);

      // note that if none of the following conditions are true
      // we don't want to change  the size of the world at all
      // of course the controls should be disabled but just in case...
      int minx = minPxcor;
      int maxx = maxPxcor;
      int miny = minPycor;
      int maxy = maxPycor;

      if (maxPxcor == -minPxcor) {
        minx = minPxcor - deltax;
        maxx = maxPxcor + deltax;
      } else if (maxPxcor == 0) {
        minx = minPxcor - deltax;
      } else if (minPxcor == 0) {
        maxx = maxPxcor + deltax;
      }

      if (maxPycor == -minPycor) {
        miny = minPycor - deltay;
        maxy = maxPycor + deltay;
      } else if (maxPycor == 0) {
        miny = minPycor - deltay;
      } else if (minPycor == 0) {
        maxy = maxPycor + deltay;
      }

      if (newSizeOK(maxx - minx, maxy - miny) &&
          (minx != minPxcor || maxx != maxPxcor ||
              miny != minPycor || maxy != maxPycor)) {
        ViewWidget viewWidget = (ViewWidget) workspace.view.getParent();
        viewWidget.settings().setDimensions(minx, maxx, miny, maxy);
        viewWidget.settings().resizeWithProgress(false); // false = no progress dialog
      }
    }
  }

  private boolean newSizeOK(int sizeX, int sizeY) {
    return sizeX >= 1 && sizeY >= 1 &&
        workspace.world.patchSize() * sizeX >= getMinimumSize().width;
  }

  private boolean checkWithUser() {
    return (!workspace.jobManager.anyPrimaryJobs())
        ||
        org.nlogo.swing.OptionDialog.show
            (this, I18N.guiJ().get("common.messages.warning"),
                "Changing the size will halt and clear the world.",
                new String[]{"Change Size", I18N.guiJ().get("common.buttons.cancel")})
            == 0;
  }

}
