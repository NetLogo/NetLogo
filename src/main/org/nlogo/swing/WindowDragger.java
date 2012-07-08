// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing;

public strictfp class WindowDragger
    extends javax.swing.JPanel
    implements
    java.awt.event.MouseListener,
    java.awt.event.MouseMotionListener {

  private final javax.swing.JWindow window;
  private final javax.swing.JLabel titleLabel = new javax.swing.JLabel();
  private java.awt.Point mousePressLoc;
  private java.awt.Point mousePressAbsLoc;
  private java.awt.Point locationWhenPressed;

  private final WindowCloseBox closeBox;

  public void setTitle(String title) {
    titleLabel.setText(title);
    revalidate();
  }

  public String getTitle() {
    return titleLabel.getText();
  }

  private static final boolean IS_MAC =
      System.getProperty("os.name").startsWith("Mac");

  public WindowDragger(javax.swing.JWindow window) {
    this.window = window;
    setBackground(java.awt.Color.WHITE);
    setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 2, 0, 2));
    titleLabel.setOpaque(true);
    titleLabel.setBackground(java.awt.Color.WHITE);

    closeBox = new WindowCloseBox();

    if (!IS_MAC) {
      java.awt.GridBagLayout gridBag = new java.awt.GridBagLayout();
      setLayout(gridBag);
      java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();

      // title label
      c.gridwidth = java.awt.GridBagConstraints.RELATIVE;
      gridBag.setConstraints(titleLabel, c);
      add(titleLabel);

      // close box
      c.gridwidth = java.awt.GridBagConstraints.REMAINDER;
      c.weightx = 1.0;
      c.anchor = java.awt.GridBagConstraints.EAST;
      gridBag.setConstraints(closeBox, c);
      add(closeBox);
    } else {
      setLayout(new org.nlogo.awt.RowLayout
          (3, java.awt.Component.LEFT_ALIGNMENT,
              java.awt.Component.CENTER_ALIGNMENT));
      add(closeBox);
      add(titleLabel);
    }
    titleLabel.setFont
        (titleLabel.getFont().deriveFont(java.awt.Font.BOLD));
    addMouseListener(this);
    addMouseMotionListener(this);
    titleLabel.addMouseListener(this);
    titleLabel.addMouseMotionListener(this);
  }

  public void mouseMoved(java.awt.event.MouseEvent e) { /* ignore */ }

  public void mouseClicked(java.awt.event.MouseEvent e) { /* ignore */ }

  public void mouseEntered(java.awt.event.MouseEvent e) { /* ignore */ }

  public void mouseExited(java.awt.event.MouseEvent e) { /* ignore */ }

  public void mouseReleased(java.awt.event.MouseEvent e) { /* ignore */ }

  public void mousePressed(java.awt.event.MouseEvent e) {
    mousePressLoc = e.getPoint();
    mousePressAbsLoc =
      org.nlogo.awt.Coordinates.convertPointToScreen(
        mousePressLoc, WindowDragger.this);
    locationWhenPressed = window.getLocationOnScreen();
  }

  public void mouseDragged(java.awt.event.MouseEvent e) {
    if (!inCloseBox(mousePressLoc)) {
      java.awt.Point dragAbsLoc =
        org.nlogo.awt.Coordinates.convertPointToScreen(
          e.getPoint(), WindowDragger.this);
      java.awt.GraphicsConfiguration gc = getGraphicsConfiguration();
      java.awt.Rectangle bounds = gc.getBounds();
      java.awt.Insets insets =
          java.awt.Toolkit.getDefaultToolkit().getScreenInsets(gc);
      bounds.x = bounds.x + insets.left;
      bounds.y = bounds.y + insets.top;
      bounds.width = bounds.width - insets.left - insets.right;
      bounds.height = bounds.height - insets.top - insets.bottom;
      int x = locationWhenPressed.x + (dragAbsLoc.x - mousePressAbsLoc.x);
      int y = locationWhenPressed.y + (dragAbsLoc.y - mousePressAbsLoc.y);
      x = StrictMath.max(bounds.x, StrictMath.min(x, (bounds.width + bounds.x)));
      y = StrictMath.max(bounds.y, StrictMath.min(y, (bounds.height + bounds.y)));
      window.setLocation(x, y);
    }
  }

  private boolean inCloseBox(java.awt.Point p) {
    // these numbers depend on where the close box is in the palette.gif file;
    // the null check is because of a bug report from a user that p was null,
    // I guess because of a bug in Swing - ST 2/27/12
    return p != null && p.x >= 7 && p.x <= 13 && p.y >= 2 && p.y <= 8;
  }

  public javax.swing.JButton getCloseBox() {
    return closeBox;
  }

  ///

  @Override
  public void paintComponent(java.awt.Graphics g) {
    super.paintComponent(g);
    g.setColor(java.awt.Color.BLACK);
    for (int x = 1; x < getWidth(); x += 2) {
      for (int y = 1; y < getHeight(); y += 2) {
        g.fillRect(x, y, 1, 1);
      }
    }
  }

}
