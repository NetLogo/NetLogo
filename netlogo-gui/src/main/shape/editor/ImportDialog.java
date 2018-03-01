// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor;

import org.nlogo.core.I18N;
import org.nlogo.core.Shape;

import java.util.ArrayList;
import java.util.List;

public strictfp class ImportDialog   // public for DeltaTick - ST 12/2/11
    extends javax.swing.JDialog
    implements javax.swing.event.ListSelectionListener {

  final ManagerDialog<? extends Shape> manager;
  final DrawableList<? extends Shape> list;

  ///

  public ImportDialog(java.awt.Dialog dialog,
               ManagerDialog<? extends Shape> manager,
               DrawableList<? extends Shape> drawableList) {
    // The Java 1.1 version of Swing doesn't allow us to pass a JDialog as the first arg to
    // the JDialog constructor, hence the necessity of passing in the frame instead - ST 3/24/02
    super(dialog, "Library", true);
    this.manager = manager;

    list = drawableList;
    list.update();

    // Create the buttons
    javax.swing.JButton importButton =
        new javax.swing.JButton("Import");
    importButton.addActionListener
        (new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            importSelectedShapes();
          }
        });
    javax.swing.Action cancelAction =
        new javax.swing.AbstractAction(I18N.guiJ().get("common.buttons.cancel")) {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            dispose();
          }
        };
    javax.swing.JButton cancelButton = new javax.swing.JButton(cancelAction);
    org.nlogo.swing.Utils.addEscKeyAction
        (this, cancelAction);

    list.addMouseListener(new javax.swing.event.MouseInputAdapter() {
      // Listen for double-clicks, and edit the selected shape
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        if (e.getClickCount() > 1) {
          importSelectedShapes();
        }
      }
    });

    // Setup the panel
    javax.swing.JPanel panel = new org.nlogo.swing.ButtonPanel
        (new javax.swing.JButton[]{importButton, cancelButton});

    // Create the scroll pane where the list will be displayed
    javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(list);

    // Add everything to the window
    getContentPane().setLayout(new java.awt.BorderLayout(0, 10));
    getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);
    getContentPane().add(panel, java.awt.BorderLayout.SOUTH);

    pack();

    // Set the window location
    setLocation(manager.getLocation().x + 10, manager.getLocation().y + 10);

    // set the default button
    getRootPane().setDefaultButton(importButton);

    setVisible(true);
  }

  // Listen for changes in list selection, and make the edit and delete buttons inoperative if necessary
  public void valueChanged(javax.swing.event.ListSelectionEvent e) {
    int[] selected = list.getSelectedIndices();
    if (selected.length == 1) {
      list.ensureIndexIsVisible(selected[0]);
    }
  }

  // Import shapes from another model
  private void importSelectedShapes() {
    Shape shape;
    String name;
    Object[] choices = {"Replace", "Rename", I18N.guiJ().get("common.buttons.cancel")};
    int[] selected = list.getSelectedIndices();
    ArrayList<Shape> shapesToAdd = new ArrayList<Shape>();

    // For each selected shape, add it to the current model's file and the turtledrawer,
    for (int i = 0; i < selected.length; ++i) {
      shape = list.getShape(selected[i]).get();

      // If the shape exists, give the user the chance to overwrite or rename
      while (manager.shapesList().exists(shape.name())) {
        int choice = javax.swing.JOptionPane.showOptionDialog
            (this,
                "A shape with the name \"" + shape.name() + "\" already exists in this model.\n" +
                    "Do you want to replace the existing shape or rename the imported one?",
                "Import",
                javax.swing.JOptionPane.YES_NO_CANCEL_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE, null,
                choices, choices[0]);

        if (choice == 0) // overwrite
        {
          shapesToAdd.add(shape);
          break;
        } else if (choice == 1) // rename
        {
          name = javax.swing.JOptionPane.showInputDialog
              (this, "Import shape as:", "Import Shapes", javax.swing.JOptionPane.PLAIN_MESSAGE);
          // if the user cancels the inputdialog, then name could
          // be null causing a nullpointerexception later on
          if (name != null) {
            shape.name_$eq(name);
          }
        } else {
          return;
        }
      }
      shapesToAdd.add(shape);
    }

    for (int i = 0; i < shapesToAdd.size(); i++) {
      manager.shapesList().addShape(shapesToAdd.get(i));
    }

    // Now update the shapes manager's list and quit this window
    manager.shapesList().update();
    manager.shapesList().selectShapeName("default");
    dispose();
  }

  // Show a warning dialog to indicate something went wrong when importing
  void sendImportWarning(String message) {
    javax.swing.JOptionPane.showMessageDialog
        (this, message, "Import", javax.swing.JOptionPane.WARNING_MESSAGE);
  }

  @Override
  public java.awt.Dimension getPreferredSize() {
    java.awt.Dimension d = super.getPreferredSize();
    d.width = StrictMath.max(d.width, 260);
    return d;
  }

}
