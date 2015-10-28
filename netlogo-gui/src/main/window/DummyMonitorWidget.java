// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.api.Editable;
import org.nlogo.core.I18N;
import org.nlogo.api.Property;

import java.util.List;

public strictfp class DummyMonitorWidget
    extends SingleErrorWidget
    implements
    Editable {

  private static final int LEFT_MARGIN = 5;
  private static final int RIGHT_MARGIN = 6;
  private static final int BOTTOM_MARGIN = 6;

  public DummyMonitorWidget() {
    super();
    setOpaque(true);
    setBackground(InterfaceColors.MONITOR_BACKGROUND);
    setBorder(widgetBorder());
    org.nlogo.awt.Fonts.adjustDefaultFont(this);
  }

  private String name = "";

  public void name(String name) {
    this.name = name;
  }

  public String name() {
    return name;
  }

  @Override
  public String displayName() {
    return name;
  }

  @Override

  public String classDisplayName() {
    return I18N.guiJ().get("tabs.run.widgets.monitor");
  }

  public List<Property> propertySet() {
    return Properties.dummyMonitor();
  }

  private static final int MIN_WIDTH = 50;
  // private static final int MIN_HEIGHT = 35 ; // no longer used
  private static final int MAX_HEIGHT = 49;

  @Override
  public java.awt.Dimension getMinimumSize() {
    return new java.awt.Dimension(MIN_WIDTH, MAX_HEIGHT);
  }

  @Override
  public java.awt.Dimension getMaximumSize() {
    return new java.awt.Dimension(10000, MAX_HEIGHT);
  }

  @Override
  public java.awt.Dimension getPreferredSize(java.awt.Font font) {
    java.awt.Dimension size = getMinimumSize();
    int pad = 12;
    java.awt.FontMetrics fontMetrics = getFontMetrics(font);
    size.width = StrictMath.max(size.width, fontMetrics.stringWidth(displayName()) + pad);
    size.height = StrictMath.max(size.height, fontMetrics.getMaxDescent() + fontMetrics.getMaxAscent() + pad);
    return size;
  }

  @Override
  public void paintComponent(java.awt.Graphics g) {
    super.paintComponent(g); // paint background
    java.awt.FontMetrics fm = g.getFontMetrics();
    int labelHeight = fm.getMaxDescent() + fm.getMaxAscent();
    java.awt.Dimension d = getSize();
    g.setColor(getForeground());
    String displayName = displayName();
    int boxHeight = (int) StrictMath.ceil(labelHeight * 1.4);
    g.drawString(displayName, LEFT_MARGIN,
        d.height - BOTTOM_MARGIN - boxHeight - fm.getMaxDescent() - 1);
    g.setColor(java.awt.Color.WHITE);
    g.fillRect(LEFT_MARGIN, d.height - BOTTOM_MARGIN - boxHeight,
        d.width - LEFT_MARGIN - RIGHT_MARGIN - 1, boxHeight);
    g.setColor(java.awt.Color.BLACK);
  }

  private int decimalPlaces = 3;

  public int decimalPlaces() {
    return decimalPlaces;
  }

  public void decimalPlaces(int decimalPlaces) {
    if (decimalPlaces != this.decimalPlaces) {
      this.decimalPlaces = decimalPlaces;
    }
  }

  @Override
  public String save() {
    StringBuilder s = new StringBuilder();
    s.append("MONITOR\n");
    s.append(getBoundsString());
    if ((null != name()) && (!name().trim().equals(""))) {
      s.append(name() + "\n");
    } else {
      s.append("NIL\n");
    }
    s.append("NIL\n");

    s.append(decimalPlaces + "\n");
    s.append("1\n");  // for compatability

    return s.toString();
  }

  @Override
  public Object load(String[] strings, Widget.LoadHelper helper) {
    String displayName = strings[5];

    if (displayName.equals("NIL")) {
      name("");
    } else {
      name(displayName);
    }
    if (strings.length > 7) {
      decimalPlaces = Integer.parseInt(strings[7]);
    }
    int x1 = Integer.parseInt(strings[1]);
    int y1 = Integer.parseInt(strings[2]);
    int x2 = Integer.parseInt(strings[3]);
    int y2 = Integer.parseInt(strings[4]);
    setSize(x2 - x1, y2 - y1);
    return this;
  }
}

