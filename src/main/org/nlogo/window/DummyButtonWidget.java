// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.api.Editable;
import org.nlogo.api.I18N;
import org.nlogo.api.Property;

import java.util.List;

public strictfp class DummyButtonWidget
    extends SingleErrorWidget
    implements Editable {
  public DummyButtonWidget() {
    setBackground(InterfaceColors.BUTTON_BACKGROUND);
    setBorder(widgetBorder());
    org.nlogo.awt.Fonts.adjustDefaultFont(this);
  }

  public List<Property> propertySet() {
    return Properties.dummyButton();
  }

  private char actionKey = 0;

  public char actionKey() {
    return actionKey;
  }

  public void actionKey(char actionKey) {
    this.actionKey = actionKey;
  }

  private String actionKeyString() {
    return actionKey == 0
        ? ""
        : Character.toString(actionKey);
  }

  private boolean keyEnabled = false;

  public boolean keyEnabled() {
    return keyEnabled;
  }

  public void keyEnabled(boolean keyEnabled) {
    if (this.keyEnabled != keyEnabled) {
      this.keyEnabled = keyEnabled;
      repaint();
    }
  }

  /// editability

  @Override
  public String classDisplayName() {
    return I18N.guiJ().get("tabs.run.widgets.button");
  }

  private String name = "";

  public void name(String name) {
    this.name = name;
    displayName(name);
  }

  public String name() {
    return name;
  }

  /// sizing

  @Override
  public java.awt.Dimension getMinimumSize() {
    return new java.awt.Dimension(55, 33);
  }

  @Override
  public java.awt.Dimension getPreferredSize(java.awt.Font font) {
    java.awt.Dimension size = getMinimumSize();
    java.awt.FontMetrics fontMetrics = getFontMetrics(font);
    size.width = StrictMath.max(size.width,
        fontMetrics.stringWidth(displayName()) + 28);
    size.height = StrictMath.max(size.height,
        fontMetrics.getMaxDescent() +
            fontMetrics.getMaxAscent() + 12);
    return size;
  }

  /// painting

  @Override
  public void paintComponent(java.awt.Graphics g) {
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    java.awt.Dimension size = getSize();
    java.awt.FontMetrics fontMetrics = g.getFontMetrics();
    int labelHeight = fontMetrics.getMaxDescent() + fontMetrics.getMaxAscent();
    String displayName = displayName();
    int availableWidth = size.width - 8;
    int stringWidth = fontMetrics.stringWidth(displayName);
    g.setColor(getForeground());

    String shortString = org.nlogo.awt.Fonts.shortenStringToFit(displayName, availableWidth, fontMetrics);
    int nx = stringWidth > availableWidth ? 4 : (size.width / 2) - (stringWidth / 2);
    int ny = (size.height / 2) + (labelHeight / 2);
    g.drawString(shortString, nx, ny);
    // now draw keyboard shortcut
    if (!actionKeyString().equals("")) {
      int ax = size.width - 4 - fontMetrics.stringWidth(actionKeyString());
      int ay = fontMetrics.getMaxAscent() + 2;
      g.setColor(keyEnabled
          ? java.awt.Color.BLACK
          : java.awt.Color.GRAY);
      g.drawString(actionKeyString(), ax - 1, ay);
    }
  }

  ///

  @Override
  public String save() {
    StringBuilder s = new StringBuilder();
    s.append("BUTTON\n");
    s.append(getBoundsString());
    if (!name().trim().equals("")) {
      s.append(name() + "\n");
    } else {
      s.append("NIL\n");
    }
    s.append("NIL\n");
    s.append("NIL\n");
    s.append(1 + "\n"); // for compatability
    s.append("T\n");  // show display name

    String temp = "OBSERVER\n"; // assume Observer button
    s.append(temp);
    s.append("NIL\n");
    if (actionKey == 0 || actionKey == ' ') {
      s.append("NIL\n");
    } else {
      s.append(actionKey + "\n");
    }
    return s.toString();
  }

  @Override
  public Object load(String[] strings, Widget.LoadHelper helper) {
    if (strings.length > 12 && !strings[12].equals("NIL")) {
      actionKey(strings[12].charAt(0));
    }

    name("");
    String dName = strings[5];
    if (!dName.equals("NIL")) {
      name(dName);
    } else {
      // to support importing old clients, sometimes there is
      // no display name but there is code intended to be used as
      // the display name ev 8/11/06
      dName = strings[6];
      if (!dName.equals("NIL")) {
        name(dName);
      }
    }

    int x1 = Integer.parseInt(strings[1]);
    int y1 = Integer.parseInt(strings[2]);
    int x2 = Integer.parseInt(strings[3]);
    int y2 = Integer.parseInt(strings[4]);
    setSize(x2 - x1, y2 - y1);
    return this;
  }

}
