// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app;

public class ModelPreview extends javax.swing.JPanel {

  // not JComponent otherwise super.paintComponent() doesn't paint the
  // background color for reasons I can't fathom - ST 8/3/03

  private java.awt.Image image = null;

  ModelPreview() {
    setBackground(java.awt.Color.BLACK);
    setOpaque(true);
    setPreferredSize(new java.awt.Dimension(400, 400));
    setMinimumSize(getPreferredSize());
    setMaximumSize(getPreferredSize());
  }

  void setImage(String imagePath) {
    image = null;
    if (imagePath != null) {
      image = org.nlogo.awt.Images.loadImageFile
          (imagePath, false); // false = don't cache
    }
    repaint();
  }

  @Override
  public void paintComponent(java.awt.Graphics g) {
    if (image == null) {
      super.paintComponent(g);
    } else {
      ((java.awt.Graphics2D) g).setRenderingHint
          (java.awt.RenderingHints.KEY_RENDERING,
              java.awt.RenderingHints.VALUE_RENDER_QUALITY);
      int width = image.getWidth(null);
      int height = image.getHeight(null);
      if (width == height) {
        g.drawImage(image, 0, 0, 400, 400, this);
      } else if (width > height) {
        g.drawImage
            (image,
                0, 0,
                (int) (width * (400.0 / height)), 400,
                this);
      } else // width < height
      {
        g.drawImage
            (image,
                0, 0,
                400, (int) (height * (400.0 / width)),
                this);
      }
    }
  }

}
