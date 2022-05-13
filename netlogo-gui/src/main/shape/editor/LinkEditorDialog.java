// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor;

import org.nlogo.core.I18N;
import org.nlogo.core.Shape;
import org.nlogo.core.ShapeList;
import org.nlogo.shape.LinkShape;

import java.util.List;
import java.util.ArrayList;

class LinkEditorDialog
    extends javax.swing.JDialog
    implements EditorDialog.VectorShapeContainer {
  private final javax.swing.JTextField name = new javax.swing.JTextField(10);
  private final javax.swing.JTextField curviness = new javax.swing.JTextField(10);

  private final List<javax.swing.JComboBox<float []>> dashes = new ArrayList<javax.swing.JComboBox<float[]>>(3);

  private final LinkShape shape;
  private final LinkShape originalShape;
  private final DrawableList<LinkShape> list;

  public void update(Shape originalShape, Shape newShape) {
    shape.directionIndicator_$eq((org.nlogo.shape.VectorShape) newShape);
  }

  public boolean exists(String name) {
    return false;
  }

  LinkEditorDialog(final DrawableList<LinkShape> list, final LinkShape shape, int x, int y) {
    super((javax.swing.JFrame) null, true);
    this.originalShape = shape;
    this.shape = (LinkShape) shape.clone();
    this.list = list;
    setResizable(false);
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(
        new java.awt.event.WindowAdapter() {
          @Override
          public void windowClosing(java.awt.event.WindowEvent e) {
            saveShape();
          }
        });

    org.nlogo.swing.Utils.addEscKeyAction
        (this,
            new javax.swing.AbstractAction() {
              public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!originalShape.toString().equals(getCurrentShape().toString())
                    && 0 != javax.swing.JOptionPane.showConfirmDialog
                    (LinkEditorDialog.this,
                        "You may lose changes made to this shape. Do you want to cancel anyway?",
                        "Confirm Cancel", javax.swing.JOptionPane.YES_NO_OPTION)) {
                  return;
                }
                dispose();
              }
            });

    java.awt.GridBagLayout gb = new java.awt.GridBagLayout();
    java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
    setLayout(gb);

    javax.swing.JLabel label = new javax.swing.JLabel("name: ");
    c.anchor = java.awt.GridBagConstraints.WEST;
    add(label, c);
    c.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    name.setText(shape.name());
    add(name, c);

    c.gridwidth = 1;
    label = new javax.swing.JLabel("direction indicator: ");
    add(label, c);
    c.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    javax.swing.JButton diButton = new javax.swing.JButton("Edit");
    add(diButton, c);
    diButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        new EditorDialog
            (LinkEditorDialog.this, shape.directionIndicator(),
                getLocation().x, getLocation().y, false);
      }
    });

    c.gridwidth = 1;
    label = new javax.swing.JLabel("curviness: ");
    add(label, c);
    c.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    curviness.setText(Double.toString(shape.curviness()));
    add(curviness, c);

    for (int i = 0; i < 3; i++) {
      javax.swing.JComboBox<float []> comboBox = new javax.swing.JComboBox<float []>(org.nlogo.shape.LinkLine$.MODULE$.dashChoices());
      comboBox.setRenderer(new DashCellRenderer());
      comboBox.setSelectedIndex(shape.getLine(i).dashIndex());
      dashes.add(comboBox);
    }

    c.gridwidth = 1;
    add(new javax.swing.JLabel("left line"), c);
    c.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    add(dashes.get(2), c);

    c.gridwidth = 1;
    add(new javax.swing.JLabel("middle line"), c);
    c.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    add(dashes.get(1), c);

    c.gridwidth = 1;
    add(new javax.swing.JLabel("right line"), c);
    c.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    add(dashes.get(0), c);

    javax.swing.JButton cancel = new javax.swing.JButton(I18N.guiJ().get("common.buttons.cancel"));
    cancel.addActionListener
        (new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            dispose();
          }
        });

    javax.swing.JButton done = new javax.swing.JButton(I18N.guiJ().get("common.buttons.ok"));
    done.addActionListener
        (new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            saveShape();
            setVisible(false);
            dispose();
          }
        });

    javax.swing.JPanel buttonPanel = new org.nlogo.swing.ButtonPanel
        (new javax.swing.JButton[]{done, cancel});

    c.anchor = java.awt.GridBagConstraints.EAST;
    add(buttonPanel, c);
    setLocation(x + 10, y + 10);

    setTitle("Link Shape");
    name.setEnabled(!ShapeList.isDefaultShapeName(shape.name()));

    list.update();
    pack();
    getRootPane().setDefaultButton(done);
    // when name is not enabled focus goes to the curviness
    // field instead ev 2/18/08
    if (ShapeList.isDefaultShapeName(shape.name())) {
      curviness.requestFocus();
    } else {
      name.requestFocus();
    }
    setVisible(true);
  }

  void saveShape() {
    String nameStr;
    // Make sure the shape has a name
    if (name.getText().equals("")) {
      nameStr =
          javax.swing.JOptionPane.showInputDialog
              (this, "Name:", "Name Shape", javax.swing.JOptionPane.PLAIN_MESSAGE);
      if (nameStr == null) {
        return;
      }
    } else {
      nameStr = name.getText();
    }

    nameStr = nameStr.toLowerCase();

    shape.name_$eq(nameStr);

    String originalName = originalShape.name();
    // If this is an attempt to overwrite a shape, prompt for
    // permission to do it
    if (list.exists(nameStr)
        && !nameStr.equals(originalName)
        && javax.swing.JOptionPane.YES_OPTION != javax.swing.JOptionPane.showConfirmDialog
        (this, "A shape with this name already exists. Do you want to replace it?",
            "Confirm Overwrite", javax.swing.JOptionPane.YES_NO_OPTION)) {
      return;
    }

    double cv = 0;
    String str = curviness.getText();

    while (str != null) {
      try {
        cv = Double.parseDouble(str);
        str = null;
      } catch (NumberFormatException e) {
        str =
            javax.swing.JOptionPane.showInputDialog
                (this, "Curviness:", "Enter a number", javax.swing.JOptionPane.PLAIN_MESSAGE);
      }
    }

    shape.curviness_$eq(cv);
    for (int i = 0; i < dashes.size(); i++) {
      int index = dashes.get(i).getSelectedIndex();
      shape.setLineVisible(i, index != 0);
      shape.getLine(i).dashes_$eq(org.nlogo.shape.LinkLine$.MODULE$.dashChoices()[index]);
    }

    list.update(originalShape, shape);
    dispose();
  }

  private LinkShape getCurrentShape() {
    LinkShape currentShape = (LinkShape) shape.clone();
    currentShape.name_$eq(name.getText());
    currentShape.curviness_$eq(Double.parseDouble(curviness.getText()));
    for (int i = 0; i < dashes.size(); i++) {
      int index = dashes.get(i).getSelectedIndex();
      currentShape.setLineVisible(i, index != 0);
      currentShape.getLine(i).dashes_$eq(org.nlogo.shape.LinkLine$.MODULE$.dashChoices()[index]);
    }
    return currentShape;
  }

  private class DashCellRenderer
      implements javax.swing.ListCellRenderer<float[]> {

    @Override public java.awt.Component getListCellRendererComponent
        (javax.swing.JList<? extends float[]> list, final float[] value,
         int index, final boolean isSelected, boolean cellHasFocus) {
      final Object obj = value;

      return new java.awt.Component() {
        private final java.awt.Dimension dimension =
            new java.awt.Dimension(85, 18);

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

        @Override
        public void paint(java.awt.Graphics g) {
          float[] arry = (float[]) obj;
          // this is a horrible hack. This configuration is supposed
          // to be blank but for some reason on Windows it's not so just
          // don't draw anything. ev 9/14/07
          if (arry.length == 2 &&
              arry[0] == 0 &&
              arry[1] == 1) {
            return;
          }
          java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
          java.awt.Dimension d = getMinimumSize();
          g2.setColor(java.awt.Color.black);
          g2.setStroke(new java.awt.BasicStroke
              (1.0f, java.awt.BasicStroke.CAP_ROUND,
                  java.awt.BasicStroke.JOIN_ROUND, 1.0f, (float[]) obj, 0));
          g2.drawLine(0, d.height / 2, d.width, d.height / 2);
        }
      };
    }
  }
}
