// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

public strictfp class AppletAdPanel
    extends javax.swing.JPanel {
  public AppletAdPanel(java.awt.event.MouseListener iconListener) {
    RotatedIconHolder icon =
        new RotatedIconHolder
            (new javax.swing.ImageIcon
                (AppletAdPanel.class.getResource("/images/icon16.gif")));
    icon.addMouseListener(iconListener);

    JVertLabel label = new JVertLabel("powered by NetLogo");
    label.setBorder
        (javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
    icon.setBorder
        (javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
    setBackground(java.awt.Color.WHITE);
    setLayout(new java.awt.BorderLayout());
    add(label, java.awt.BorderLayout.CENTER);
    add(icon, java.awt.BorderLayout.SOUTH);
  }

  public class JVertLabel
      extends javax.swing.JLabel {
    public JVertLabel(String label) {
      super(label);
      setFont
          (new java.awt.Font(org.nlogo.awt.Fonts.platformFont(),
              java.awt.Font.PLAIN, 10));
    }

    @Override
    public java.awt.Dimension getPreferredSize() {
      java.awt.Dimension size = super.getPreferredSize();
      return new java.awt.Dimension(size.height, size.width);
    }

    @Override
    public java.awt.Dimension getMaximumSize() {
      java.awt.Dimension size = super.getMaximumSize();
      return new java.awt.Dimension(size.height, size.width);
    }

    @Override
    public java.awt.Dimension getMinimumSize() {
      java.awt.Dimension size = super.getMinimumSize();
      return new java.awt.Dimension(size.height, size.width);
    }

    @Override
    public void paintComponent(java.awt.Graphics g) {
      java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;

      g2d.rotate(StrictMath.toRadians(90.0));
      g2d.drawString(getText(), 2, -5);
    }
  }

  public class RotatedIconHolder
      extends javax.swing.JLabel {
    public RotatedIconHolder(javax.swing.Icon icon) {
      setIcon(icon);
    }

    @Override
    public java.awt.Dimension getPreferredSize() {
      java.awt.Dimension size = super.getPreferredSize();
      return new java.awt.Dimension(size.height, size.width);
    }

    @Override
    public java.awt.Dimension getMaximumSize() {
      java.awt.Dimension size = super.getMaximumSize();
      return new java.awt.Dimension(size.height, size.width);
    }

    @Override
    public java.awt.Dimension getMinimumSize() {
      java.awt.Dimension size = super.getMinimumSize();
      return new java.awt.Dimension(size.height, size.width);
    }

    @Override
    public void paintComponent(java.awt.Graphics g) {
      java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;

      g2d.rotate(StrictMath.toRadians(90.0));
      getIcon().paintIcon(this, g, 2, (-getWidth()) + 2);
    }
  }
}
