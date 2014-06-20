// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor;

import org.nlogo.shape.DrawableShape;

public strictfp class ShapeCellRenderer   // public for DeltaTick - ST 12/2/11
    extends javax.swing.JPanel
    implements javax.swing.ListCellRenderer<String> {

  protected Object theShape;
  protected final java.awt.Component shapeComponent;
  protected final java.awt.Dimension dimension =
      new java.awt.Dimension(90, 34);
  protected final javax.swing.JLabel shapeName =
      new javax.swing.JLabel("blah", javax.swing.SwingConstants.LEFT);
  final DrawableList list;

  public ShapeCellRenderer(DrawableList list) {
    this.list = list;
    shapeComponent = getShapeComponent();
    setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));
    add(shapeComponent);
    add(javax.swing.Box.createHorizontalStrut(20));
    add(shapeName);
    add(javax.swing.Box.createHorizontalGlue());
  }

  public java.awt.Component getShapeComponent() {
    return new java.awt.Component() {
      @Override
      public java.awt.Dimension getMinimumSize() {
        return dimension;
      }

      @Override
      public java.awt.Dimension getPreferredSize() {
        return dimension;
      }

      @Override
      public java.awt.Dimension getMaximumSize() {
        return dimension;
      }

      private void preview(java.awt.Graphics2D g, java.awt.Shape clip,
                           int left, int top, int size) {
        g.setColor(getForeground());
        if (((DrawableShape) theShape).isRotatable()) {
          g.fillOval(left - 1, top - 1, size + 1, size + 1);
        } else {
          g.fillRect(left - 1, top - 1, size + 2, size + 2);
        }
        g.clipRect(left, top, size, size);
        ((DrawableShape) theShape).paint
            (new org.nlogo.api.Graphics2DWrapper(g),
                EditorDialog.getColor(((DrawableShape) theShape).getEditableColorIndex()),
                left, top, size, 0);
        g.setClip(clip);
      }

      @Override
      public void paint(java.awt.Graphics g) {
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
        g.setColor(getBackground());
        g.fillRect(1, 1, dimension.width - 2, dimension.height - 2);
        g2.setRenderingHint
            (java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        java.awt.Shape clip = g.getClip();
        preview(g2, clip, 2, 12, 9);
        preview(g2, clip, 16, 11, 12);
        preview(g2, clip, 33, 7, 20);
        preview(g2, clip, 58, 2, 30);
      }
    };
  }

  // Method that actually renders the item
  @Override public java.awt.Component getListCellRendererComponent
  (javax.swing.JList<? extends String> list, String value, int index,
   boolean isSelected, boolean cellHasFocus) {
    theShape = this.list.getShape(index);
    shapeName.setText(value.toString());
    if (isSelected) {
      setOpaque(true);
      shapeName.setForeground(list.getSelectionForeground());
      setBackground(list.getSelectionBackground());
      shapeComponent.setForeground(list.getSelectionBackground());
      shapeComponent.setBackground(list.getSelectionBackground());
    } else {
      setOpaque(false);
      shapeName.setForeground(list.getForeground());
      setBackground(null);
      shapeComponent.setForeground(java.awt.Color.BLACK);
      shapeComponent.setBackground(null);
    }
    return this;
  }

}
