package org.nlogo.app;

/*
 * I ripped out this class from ModelsLibraryDialog.java to make it accessible from
 * `previewCommands.PreviewPanel`. Besides making it public, the only modification is 
 * the addition of the `void setImage(java.awt.Image newImage)` method.
 * This should be converted to Scala when we deal with
 * https://github.com/NetLogo/models/issues/48
 * -- NP 2015-04-25
 */
public strictfp class GraphicsPreview extends javax.swing.JPanel {
    // not JComponent otherwise super.paintComponent() doesn't paint the
    // background color for reasons I can't fathom - ST 8/3/03

    private java.awt.Image image = null;

    public GraphicsPreview() {
      setBackground(java.awt.Color.BLACK);
      setOpaque(true);
      setPreferredSize(new java.awt.Dimension(400, 400));
      setMinimumSize(getPreferredSize());
      setMaximumSize(getPreferredSize());
    }

    public void setImage(String imagePath) {
      image = null;
      if (imagePath != null) {
        image = org.nlogo.awt.Images.loadImageFile
            (imagePath, false); // false = don't cache
      }
      repaint();
    }
    
    public void setImage(java.awt.Image newImage) {
      image = newImage;
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
