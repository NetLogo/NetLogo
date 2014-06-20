// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor;

// Class that handles the rendering of items in the colorSelection
// ComboBox.  It displays a rectangle filled with the items color,
// next to text consisting of the name of that color

strictfp class ColorCellRenderer
    extends javax.swing.JPanel
    implements javax.swing.ListCellRenderer<Integer> {

  private static final String[] NAMES =
      {"Gray", "Red", "Orange", "Brown", "Yellow", "Green",
          "Lime", "Turquoise", "Cyan", "Sky", "Blue", "Violet",
          "Magenta", "Pink", "Black", "White"};

  private final org.nlogo.swing.ColorSwatch colorSwatch;
  private final javax.swing.JLabel colorName = new javax.swing.JLabel();

  ColorCellRenderer() {
    setOpaque(true);
    setLayout(new javax.swing.BoxLayout
        (this, javax.swing.BoxLayout.X_AXIS));
    setBorder(new javax.swing.border.LineBorder
        (java.awt.Color.WHITE, 0));
    colorSwatch =
        new org.nlogo.swing.ColorSwatch
            (20, colorName.getPreferredSize().height);
    colorSwatch.setOpaque(true);
    add(colorSwatch);
    add(javax.swing.Box.createHorizontalGlue());
    add(colorName);
    add(javax.swing.Box.createHorizontalStrut(5));
  }

  // method that actually renders the item
  public java.awt.Component getListCellRendererComponent
  (javax.swing.JList<? extends Integer> list, Integer value, int index,
   boolean isSelected, boolean cellHasFocus) {
    colorSwatch.setBackground(EditorDialog.getColor(value));
    colorName.setText(NAMES[value]);
    if (isSelected) {
      colorName.setForeground(list.getSelectionForeground());
      setBackground(list.getSelectionBackground());
    } else {
      colorName.setForeground(list.getForeground());
      setBackground(list.getBackground());
    }
    return this;
  }

}
