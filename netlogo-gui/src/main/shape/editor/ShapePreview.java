// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor;

import org.nlogo.shape.Element;
import org.nlogo.shape.VectorShape;

import java.util.Observable;

strictfp class ShapePreview
    extends javax.swing.JPanel
    implements java.util.Observer {

  // Information regarding how and whether to rotate the shape
  private int rotationAngle;
  private final int rotationSpeed;
  private boolean stopRotation = false;      // Flag indicating whether preview is in "stop rotation mode"

  // Information about the size of the shape and the shapes editor it belongs to
  private final int scale;

  // the shape being previewed
  private final org.nlogo.shape.VectorShape shape;

  // Timer so we keep rotating
  javax.swing.Timer timer =
      new javax.swing.Timer
          (30,
              new javax.swing.AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                  rotate();
                }
              });

  ///

  // Creates a new ShapePreview, given information about how big the preview should be and how it
  // should be rotated
  ShapePreview(VectorShape shape, int scale, int rotationSpeed) {
    this.shape = shape;
    this.scale = scale;
    this.rotationSpeed = rotationSpeed;
    rotationAngle = 0;
    setBackground(java.awt.Color.DARK_GRAY);
  }

  // Set the size of the preview
  @Override
  public java.awt.Dimension getPreferredSize() {
    return new java.awt.Dimension(60, 78);
  }

  @Override
  public java.awt.Dimension getMinimumSize() {
    return new java.awt.Dimension(60, 78);
  }

  @Override
  public java.awt.Dimension getMaximumSize() {
    return new java.awt.Dimension(60, 78);
  }

  // Method called by VectorShape when it has changed
  public void update(Observable o, Object rect) {
    repaint();
  }

  // Draws a preview of the current shape, rotated around the origin
  @Override
  public void paintComponent(java.awt.Graphics g) {
    // Fill in background
    super.paintComponent(g);

    // Calculate where to draw the shape from.
    int x = Element.round(.5 * getWidth() - (.5 * scale));
    int y = x;
    g.setColor(java.awt.Color.BLACK);
    g.fillRect(x, y, scale, scale);
    g.setFont
        (new java.awt.Font(org.nlogo.awt.Fonts.platformFont(),
            java.awt.Font.PLAIN, 10));
    g.drawString(Integer.toString(scale),
        (getWidth() - g.getFontMetrics().stringWidth(Integer.toString(scale))) / 2,
        y + scale + g.getFontMetrics().getHeight() + 2);
    g.clipRect(x, y, scale, scale);
    synchronized (shape)    // Draw the shape at the current angle
    {
      org.nlogo.api.Graphics2DWrapper g2
          = new org.nlogo.api.Graphics2DWrapper((java.awt.Graphics2D) g);
      g2.antiAliasing(true);
      shape.paint(g2,
          new java.awt.Color
              (org.nlogo.api.Color.getARGBbyPremodulatedColorNumber(colorNumber)),
          x, y, scale, rotationAngle);
      shape.setRotatable(true);    // Ensure that even if the model isn't rotatable, its preview is (which is
    }                    // relevant from the time 'rotatable' is unchecked to when the angle hits 0)
  }

  // Method called by EditorDialog whenever the current shape has become rotatable or not rotatable
  void updateRotation(boolean rotatable) {
    stopRotation = !rotatable;
  }

  @Override
  public void addNotify() {
    super.addNotify();
    timer.start();
  }

  @Override
  public void removeNotify() {
    timer.stop();
    super.removeNotify();
  }

  private int colorNumber = 5;

  // for cycling the color once a second
  private int newColorNumber() {
    return 5 + 10 * (int) ((long) StrictMath.floor(System.currentTimeMillis() / 200L)
        % 14L);
  }

  // Method run by the timer, which animates a rotating preview of the shape
  private void rotate() {
    int lastColorNumber = colorNumber;
    colorNumber = newColorNumber();
    if (colorNumber != lastColorNumber) {
      shape.markRecolorableElements(EditorDialog.getColor(shape.getEditableColorIndex()),
          shape.getEditableColorIndex());
      repaint();
    }
    if (!stopRotation || rotationAngle != 0) {
      if (stopRotation)            // If we're in stopRotation mode, speed up
      {
        int fastSpeed;
        if (rotationSpeed > 0) {
          fastSpeed = 16;
        } else {
          fastSpeed = -16;
        }
        int newRotationAngle = (rotationAngle + fastSpeed + 360) % 360;
        if (newRotationAngle - rotationAngle != fastSpeed) {
          rotationAngle = 0;
        } else {
          rotationAngle = newRotationAngle;
        }
      } else {
        rotationAngle = ((rotationAngle + rotationSpeed) + 360) % 360;
      }
      repaint();
    }
  }

}
