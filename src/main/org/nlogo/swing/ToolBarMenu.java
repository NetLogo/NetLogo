// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing;

public abstract strictfp class ToolBarMenu
    extends javax.swing.JPanel {

  private final String name;

  public ToolBarMenu(String name) {
    this.name = name;
    setBorder(Utils.createWidgetBorder());
    if (System.getProperty("os.name").startsWith("Mac")) {
      setBackground(java.awt.Color.WHITE);
    }
    addMouseListener
        (new java.awt.event.MouseAdapter() {
          @Override
          public void mousePressed(java.awt.event.MouseEvent e) {
            WrappingPopupMenu menu = new WrappingPopupMenu();
            populate(menu);
            menu.show(ToolBarMenu.this, 0, getHeight());
          }
        });
    org.nlogo.awt.Fonts.adjustDefaultFont(this);
  }

  protected abstract void populate(javax.swing.JPopupMenu menu);

  ///

  @Override
  public java.awt.Dimension getMinimumSize() {
    return new java.awt.Dimension(11, 20);
  }

  @Override
  public java.awt.Dimension getPreferredSize() {
    java.awt.Dimension size = getMinimumSize();
    int xpad = 5;
    int ypad = 2;
    java.awt.FontMetrics fontMetrics = getFontMetrics(getFont());
    size.width = StrictMath.max
        (size.width, fontMetrics.stringWidth(name) + 2 * xpad + 11);
    size.height = StrictMath.max
        (size.height, fontMetrics.getMaxDescent() + fontMetrics.getMaxAscent() + 2 * ypad);
    return size;
  }

  @Override
  public void paintComponent(java.awt.Graphics g) {
    java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
    g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
    super.paintComponent(g);
    // Draw Label
    g.setColor(getForeground());
    java.awt.FontMetrics fontMetrics = g.getFontMetrics();
    g.drawString(name, 5, fontMetrics.getMaxAscent() + 2);

    // Draw Arrow
    int[] xpnts = new int[]
        {getWidth() - 13, getWidth() - 9, getWidth() - 5};
    int[] ypnts = new int[]
        {(getHeight() / 2) - 2, (getHeight() / 2) + 2, (getHeight() / 2) - 2};
    g.fillPolygon(xpnts, ypnts, 3);
  }

}
