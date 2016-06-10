// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.core.I18N;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

public strictfp class SpeedSliderPanel
    extends javax.swing.JPanel
    implements
    java.awt.event.MouseListener,
    javax.swing.event.ChangeListener,
    org.nlogo.window.Events.LoadBeginEvent.Handler {
  private final GUIWorkspace workspace;

  final SpeedSlider speedSlider;

  final javax.swing.JLabel normal = new SpeedLabel(I18N.guiJ().get("tabs.run.speedslider.normalspeed"));

  private final TickCounterLabel tickCounter;

  public SpeedSliderPanel(GUIWorkspace workspace, TickCounterLabel tickCounter) {
    this.workspace = workspace;
    this.tickCounter = tickCounter;
    speedSlider = new SpeedSlider((int) workspace.speedSliderPosition());
    speedSlider.setFocusable(false);
    speedSlider.addChangeListener(this);
    speedSlider.addMouseListener(this);
    speedSlider.setOpaque(false);
    org.nlogo.awt.Fonts.adjustDefaultFont(normal);
    setOpaque(false);
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout(gridbag);
    c.fill = GridBagConstraints.VERTICAL;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.gridy = 0;
    c.anchor = GridBagConstraints.PAGE_START;
    add(normal, c);
    c.fill = GridBagConstraints.VERTICAL;
    c.anchor = GridBagConstraints.CENTER;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.weighty = 0.25;
    c.gridy = 1;
    add(speedSlider, c);
    c.fill = GridBagConstraints.VERTICAL;
    c.anchor = GridBagConstraints.PAGE_END;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.gridy = 2;
    add(tickCounter, c);
    enableLabels(0);
  }

  @Override
  public void setEnabled(boolean enabled) {
    speedSlider.setEnabled(enabled);
    // if we do setVisible() on the label, that changes the layout
    // which is a bit jarring, so do this instead - ST 9/16/08
    if (enabled) {
      stateChanged(null);
    } else {
      normal.setText(" ");
    }
  }

  public void stateChanged(javax.swing.event.ChangeEvent e) {
    int value = speedSlider.getValue();
    // adjust the speed reported to the workspace
    // so there isn't a big gap between the snap area
    // and outside the snap area. ev 2/22/07
    if (value < -10) {
      value += 10;
    } else if (value > 10) {
      value -= 10;
    } else {
      value = 0;
    }
    workspace.speedSliderPosition(value / 2);
    if (org.nlogo.api.Version$.MODULE$.isLoggingEnabled()) {
      org.nlogo.log.Logger.logSpeedSlider(value);
    }
    enableLabels(value);
    workspace.updateManager().nudgeSleeper();
  }

  void enableLabels(int value) {
    if (value == 0) {
      normal.setText("      " + I18N.guiJ().get("tabs.run.speedslider.normalspeed"));
    } else if (value < 0) {
      normal.setText(I18N.guiJ().get("tabs.run.speedslider.slower") + "                         ");
    } else {
      normal.setText("                         " + I18N.guiJ().get("tabs.run.speedslider.faster"));
    }
  }

  // mouse listener junk
  public void mouseClicked(java.awt.event.MouseEvent e) {
  }

  public void mousePressed(java.awt.event.MouseEvent e) {
  }

  public void mouseEntered(java.awt.event.MouseEvent e) {
  }

  public void mouseExited(java.awt.event.MouseEvent e) {
  }

  // when we release the mouse if it's kinda close to the
  // center snappy snap.  ev 2/22/07
  public void mouseReleased(java.awt.event.MouseEvent e) {
    int value = speedSlider.getValue();
    if ((value <= 10 && value > 0) ||
        (value >= -10 && value < 0)) {
      speedSlider.setValue(0);
    }
  }

  public void handle(org.nlogo.window.Events.LoadBeginEvent e) {
    speedSlider.reset();
  }

  public void setValue(int speed) {
    if (speedSlider.getValue() != speed) {
      speedSlider.setValue(speed);
    }
    enableLabels((int) workspace.speedSliderPosition());
  }

  public int getValue() {
    return speedSlider.getValue();
  }

  public int getMaximum() {
    return speedSlider.getMaximum();
  }

  @Override
  public boolean isEnabled() {
    return speedSlider.isEnabled();
  }

  private class SpeedSlider
      extends javax.swing.JSlider
      implements java.awt.event.MouseWheelListener {
    SpeedSlider(int defaultSpeed) {
      super(-110, 112, defaultSpeed);
      setExtent(1);
      setToolTipText("Adjust speed of model");
      addMouseWheelListener(this);
    }

    @Override
    public java.awt.Dimension getPreferredSize() {
      return new java.awt.Dimension
          (180, super.getPreferredSize().height);
    }

    @Override
    public java.awt.Dimension getMinimumSize() {
      return new java.awt.Dimension
          (60, super.getPreferredSize().height);
    }

    void reset() {
      setValue(0);
    }

    public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
      setValue(getValue() - e.getWheelRotation());
    }

    @Override
    public void paint(java.awt.Graphics g) {
      java.awt.Rectangle bounds = getBounds();
      int x = bounds.x + (bounds.width / 2) - 1;
      g.setColor(java.awt.Color.gray);
      g.drawLine(x, bounds.y - (bounds.height * 1/2), x, bounds.y - (bounds.height * 1 / 4));
      super.paint(g);
    }
  }

  private class SpeedLabel
      extends javax.swing.JLabel {
    SpeedLabel(String label) {
      super(label);
    }

    @Override
    public java.awt.Dimension getPreferredSize() {
      return getMinimumSize();
    }

    @Override
    public java.awt.Dimension getMinimumSize() {
      java.awt.Dimension d = super.getMinimumSize();
      java.awt.FontMetrics fontMetrics = getFontMetrics(getFont());
      d.width = StrictMath.max(d.width,
          fontMetrics.stringWidth
          ("                         " + I18N.guiJ().get("tabs.run.speedslider.faster")) + 10);

      return d;
    }
  }
}
