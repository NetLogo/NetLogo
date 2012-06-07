// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing;

strictfp class WindowCloseBox
    extends javax.swing.JButton
    implements java.awt.event.ActionListener {

  private static final boolean IS_MAC =
      System.getProperty("os.name").startsWith("Mac");

  private boolean inBounds = false;

  WindowCloseBox() {
    setBackground(IS_MAC
        ? java.awt.Color.LIGHT_GRAY
        : java.awt.Color.WHITE);
    addActionListener(this);
    addMouseListener
        (new java.awt.event.MouseAdapter() {
          @Override
          public void mouseEntered(java.awt.event.MouseEvent e) {
            inBounds = true;
            repaint();
          }

          @Override
          public void mouseExited(java.awt.event.MouseEvent e) {
            inBounds = false;
            repaint();
          }
        });
  }

  public void actionPerformed(java.awt.event.ActionEvent e) {
    java.awt.Window window = org.nlogo.awt.Hierarchy.getWindow(this);
    window.dispatchEvent
        (new java.awt.event.WindowEvent
            (window, java.awt.event.WindowEvent.WINDOW_CLOSING));
  }

  @Override
  public java.awt.Dimension getMinimumSize() {
    return IS_MAC
        ? new java.awt.Dimension(7, 7)
        : new java.awt.Dimension(9, 8);
  }

  @Override
  public java.awt.Dimension getPreferredSize() {
    return getMinimumSize();
  }

  @Override
  public boolean isFocusable() {
    return false;
  }

  @Override
  public void paintComponent(java.awt.Graphics g) {
    if (IS_MAC) {
      g.setColor
          (inBounds || isSelected()
              ? java.awt.Color.DARK_GRAY
              : getBackground());
      g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
      g.setColor(java.awt.Color.BLACK);
      g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    } else {
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
      g.setColor(java.awt.Color.BLACK);
      g.drawLine(1, 1, getWidth() - 3, getHeight() - 2);
      g.drawLine(2, 1, getWidth() - 2, getHeight() - 2);
      g.drawLine(getWidth() - 3, 1, 1, getHeight() - 2);
      g.drawLine(getWidth() - 2, 1, 2, getHeight() - 2);
    }
  }
}
