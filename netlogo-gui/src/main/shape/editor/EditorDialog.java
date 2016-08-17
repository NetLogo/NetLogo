// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.nlogo.core.I18N;
import org.nlogo.core.Shape;
import org.nlogo.shape.Circle;
import org.nlogo.shape.Element;
import org.nlogo.shape.Line;
import org.nlogo.shape.Polygon;
import org.nlogo.shape.Rectangle;
import org.nlogo.shape.VectorShape;

strictfp class EditorDialog
    extends javax.swing.JDialog
    implements java.util.Observer

{

  private final VectorShape originalShape;
  private final VectorShape shape;
  private final VectorShapeContainer container;
  private final ShapeView shapeView;

  private Class<? extends Element> elementType = null;
  private java.awt.Color elementColor;
  //private boolean shapeChanged = false ;
  private boolean editingElements = false;

  private final ShapePreview[] previews;
  private final org.nlogo.swing.TextField nameText;

  private final javax.swing.JToggleButton editElements;
  private final javax.swing.JButton deleteSelected, duplicateSelected,
      bringToFront, sendToBack, undoButton;

  // this is where we store the last edit, that we need to able to
  // do.  UndoableEdit is an interface; below we declare various
  // concrete subclasses of AbstractUndoableEdit, which implements
  // UndoableEdit.  This is standard Swing undo API stuff.
  // - SAB/ST 6/11/04
  private javax.swing.undo.UndoableEdit undoableEdit;

  private final javax.swing.JComboBox<Integer> colorSelection;

  private boolean fillShapes = true;
  private boolean shapeRotatable = true;
  private boolean snapToGrid = true;

  ///

  EditorDialog(VectorShapeContainer cont,
               final VectorShape originalShape,
               int x, int y, boolean nameEditable) {
    super((javax.swing.JFrame) null, true);
    this.container = cont;
    this.originalShape = originalShape;

    // edit a copy, not the original
    shape = originalShape.clone();
    shape.addObserver(this);
    shapeView = new ShapeView(this, shape);
    shape.addObserver(shapeView);
    setResizable(false);
    AbstractAction closingAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        if (!originalShape.toString().equals(getCurrentShape().toString()) &&
            0 != JOptionPane.showConfirmDialog(EditorDialog.this,
                "You may lose changes made to this shape. Do you want to cancel anyway?",
                "Confirm Cancel", javax.swing.JOptionPane.YES_NO_OPTION)) {
          return;
        }
        dispose();
      }
    };
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(
        new java.awt.event.WindowAdapter() {
          @Override
          public void windowClosing(java.awt.event.WindowEvent e) {
            closingAction.actionPerformed(null);
          }
        });

    org.nlogo.swing.Utils.addEscKeyAction(this, closingAction);

    javax.swing.JPanel leftPanel = new javax.swing.JPanel();
    leftPanel.setLayout(new org.nlogo.awt.ColumnLayout
        (0,
            java.awt.Component.CENTER_ALIGNMENT,
            java.awt.Component.TOP_ALIGNMENT));

    javax.swing.JPanel rightPanel = new javax.swing.JPanel();
    rightPanel.setLayout(new org.nlogo.awt.ColumnLayout
        (0,
            java.awt.Component.CENTER_ALIGNMENT,
            java.awt.Component.TOP_ALIGNMENT));

    // EDITING TOOL BAR
    javax.swing.JToolBar editingToolBar = new javax.swing.JToolBar();
    editingToolBar.setFloatable(false);
    editingToolBar.setLayout(new java.awt.GridLayout(4, 2));
    javax.swing.ButtonGroup editingToolGroup =
        new javax.swing.ButtonGroup();

    // edit
    editElements = new javax.swing.JToggleButton
        (new javax.swing.ImageIcon
            (EditorDialog.class.getResource
                ("/images/shapes-editor/arrow.gif")), false);
    editElements.setToolTipText("Select");
    editElements.addActionListener
        (new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            editingElements = editElements.isSelected();
            shapeView.deselectAll();
            deleteSelected.setEnabled(false);
            duplicateSelected.setEnabled(false);
            if (editingElements) {
              shapeView.setCursor
                  (java.awt.Cursor.getPredefinedCursor
                      (java.awt.Cursor.DEFAULT_CURSOR));
            } else {
              shapeView.setCursor
                  (java.awt.Cursor.getPredefinedCursor
                      (java.awt.Cursor.CROSSHAIR_CURSOR));
            }
            if (shapeView.drawingPolygon()) {
              shapeView.selfFinishPolygon(true);
            }
          }
        });
    editingToolGroup.add(editElements);
    editingToolBar.add(editElements);

    // delete selected
    deleteSelected = new javax.swing.JButton("Delete");
    deleteSelected.setEnabled(false);
    deleteSelected.addActionListener
        (new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            undoableEdit = new UndoableDeleteEdit
                (shapeView.getSelectedElement(),
                    shape.getElements().indexOf
                        (shapeView.getSelectedElement()));
            undoButton.setEnabled(undoableEdit.canUndo());
            shape.remove(shapeView.getSelectedElement());
            shapeView.deselectAll();
            if (shape.getElements().isEmpty()) {
              // last element was deleted
              deleteSelected.setEnabled(false);
              bringToFront.setEnabled(false);
              sendToBack.setEnabled(false);
              shape.changed();
            }
          }
        });

    // duplicate selected
    duplicateSelected = new javax.swing.JButton("Duplicate");
    duplicateSelected.setEnabled(false);
    duplicateSelected.addActionListener
        (new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            if (shapeView.getSelectedElement() != null) {
              Element newElement =
                  (Element) shapeView.getSelectedElement().clone();
              shape.add(newElement);
              makeUndoableDraw(newElement);
            }
          }
        });

    // bring to front
    bringToFront = new javax.swing.JButton("Bring to front");
    bringToFront.setEnabled(false);
    bringToFront.addActionListener
        (new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            makeUndoableModification
                (shapeView.getSelectedElement(),
                    shape.getElements().indexOf
                        (shapeView.getSelectedElement()));
            shape.remove(shapeView.getSelectedElement());
            shape.add(shapeView.getSelectedElement());
          }
        });

    // send to back
    sendToBack = new javax.swing.JButton("Send to back");
    sendToBack.setEnabled(false);
    sendToBack.addActionListener
        (new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            makeUndoableModification
                (shapeView.getSelectedElement(),
                    shape.getElements().indexOf
                        (shapeView.getSelectedElement()));
            shape.remove(shapeView.getSelectedElement());
            shape.addAtPosition
                (0, shapeView.getSelectedElement());
          }
        });

    // undo button
    undoButton = new javax.swing.JButton("Undo");
    undoButton.setEnabled(false);
    undoButton.addActionListener
        (new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            undoableEdit.undo();
            undoButton.setEnabled(undoableEdit.canUndo());
            shapeView.deselectAll();
          }
        });

    // drawing tools
    addToolBarButton
        (editingToolBar, editingToolGroup,
            new CreateAction("line", Line.class,
                "Draw line", false));

    addToolBarButton
        (editingToolBar, editingToolGroup,
            new CreateAction("rectangle-filled", Rectangle.class,
                "Draw filled rectangle", true));

    addToolBarButton
        (editingToolBar, editingToolGroup,
            new CreateAction("rectangle", Rectangle.class,
                "Draw rectangle", false));

    addToolBarButton
        (editingToolBar, editingToolGroup,
            new CreateAction("circle-filled", Circle.class,
                "Draw filled circle", true));

    addToolBarButton
        (editingToolBar, editingToolGroup,
            new CreateAction("circle", Circle.class,
                "Draw circle", false));

    addToolBarButton
        (editingToolBar, editingToolGroup,
            new CreateAction("polygon-filled", Polygon.class,
                "Draw filled polygon", true));

    addToolBarButton
        (editingToolBar, editingToolGroup,
            new CreateAction("polygon", Polygon.class,
                "Draw polygon", false));

    // Add the ComboBox to allow users to specify which color is
    // recolorable
    List<Integer> colors =
        new ArrayList<Integer>
            (org.nlogo.api.Color.ColorNames().length);
    javax.swing.ButtonGroup colorGroup =
        new javax.swing.ButtonGroup();
    javax.swing.JToolBar colorGrid = new javax.swing.JToolBar();
    colorGrid.setFloatable(false);
    colorGrid.setLayout(new java.awt.GridLayout(4, 4));

    for (int icolor = 0;
         icolor < org.nlogo.api.Color.ColorNames().length;
         icolor++) {
      String userstr =
          "Draw in " + org.nlogo.api.Color.getColorNameByIndex(icolor);
      javax.swing.AbstractButton button =
          addToolBarButton
              (colorGrid,
                  colorGroup,
                  new ColorAction
                      (org.nlogo.api.Color.getColorNameByIndex(icolor),
                          new java.awt.Color
                              (org.nlogo.api.Color.getARGBByIndex(icolor)),
                          userstr));
      if (icolor == shape.getEditableColorIndex()) {
        button.setSelected(true);
      }
      colors.add(Integer.valueOf(icolor));
    }

    colorSelection = new javax.swing.JComboBox<Integer>(colors.toArray(new Integer[0]));
    colorSelection.setRenderer(new ColorCellRenderer());
    colorSelection.addActionListener
        (new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            setEditableColor();
          }
        });

    // snap to grid
    final javax.swing.JCheckBox snapToGridButton =
        new javax.swing.JCheckBox("Snap to grid", true);
    snapToGridButton.addActionListener
        (new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            snapToGrid = snapToGridButton.isSelected();
          }
        });

    // rotatable
    final javax.swing.JCheckBox rotatableButton =
        new javax.swing.JCheckBox("Rotatable", true);
    rotatableButton.addActionListener
        (new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            shapeRotatable = rotatableButton.isSelected();
            for (int i = 0; i < previews.length; i++) {
              previews[i].updateRotation(shapeRotatable);
            }
            shape.setRotatable(shapeRotatable);
            shapeView.repaint();
          }
        });

    // rotate left button
    javax.swing.JButton rotateLeftButton = new javax.swing.JButton("Rotate Left");
    rotateLeftButton.addActionListener
        (new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            if (shapeView.hasSelectedElement()) {
              makeUndoableModification
                  (shapeView.getSelectedElement(),
                      shape.getElements().indexOf
                          (shapeView.getSelectedElement()));
              shapeView.getSelectedElement().rotateLeft();
            } else {
              undoableEdit = null;
              undoButton.setEnabled(false);
              shape.rotateLeft();
            }
            shapeView.repaint();
          }
        });

    // rotate right button
    javax.swing.JButton rotateRightButton = new javax.swing.JButton("Rotate Right");
    rotateRightButton.addActionListener
        (new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            if (shapeView.hasSelectedElement()) {
              makeUndoableModification
                  (shapeView.getSelectedElement(),
                      shape.getElements().indexOf
                          (shapeView.getSelectedElement()));
              shapeView.getSelectedElement().rotateRight();
            } else {
              undoableEdit = null;
              undoButton.setEnabled(false);
              shape.rotateRight();
            }
            shapeView.repaint();
          }
        });

    // flip horizontal button
    javax.swing.JButton flipHorizontalButton = new javax.swing.JButton("Flip Horizontal");
    flipHorizontalButton.addActionListener
        (new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            if (shapeView.hasSelectedElement()) {
              makeUndoableModification
                  (shapeView.getSelectedElement(),
                      shape.getElements().indexOf
                          (shapeView.getSelectedElement()));
              shapeView.getSelectedElement().flipHorizontal();
            } else {
              undoableEdit = null;
              undoButton.setEnabled(false);
              shape.flipHorizontal();
            }
            shapeView.repaint();
          }
        });

    // flip vertical button
    javax.swing.JButton flipVerticalButton = new javax.swing.JButton("Flip Vertical");
    flipVerticalButton.addActionListener
        (new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            if (shapeView.hasSelectedElement()) {
              makeUndoableModification
                  (shapeView.getSelectedElement(),
                      shape.getElements().indexOf
                          (shapeView.getSelectedElement()));
              shapeView.getSelectedElement().flipVertical();
            } else {
              undoableEdit = null;
              undoButton.setEnabled(false);
              shape.flipVertical();
            }
            shapeView.repaint();
          }
        });

    // fill left panel
    leftPanel.add(editingToolBar);
    leftPanel.add(snapToGridButton);
    leftPanel.add(colorGrid);
    leftPanel.add(javax.swing.Box.createVerticalStrut(10));
    leftPanel.add(new javax.swing.JLabel("Color that changes:"));
    leftPanel.add(javax.swing.Box.createVerticalStrut(3));
    leftPanel.add(colorSelection);
    leftPanel.add(deleteSelected);
    leftPanel.add(duplicateSelected);
    leftPanel.add(bringToFront);
    leftPanel.add(sendToBack);
    leftPanel.add(undoButton);
    leftPanel.add(rotatableButton);

    // fill right panel
    rightPanel.add(rotateLeftButton);
    rightPanel.add(rotateRightButton);
    rightPanel.add(flipHorizontalButton);
    rightPanel.add(flipVerticalButton);

    // BOTTOM BUTTONS

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
          }
        });


    // PREVIEWS
    previews = new ShapePreview[5];
    previews[0] = new ShapePreview(shape, 9, 5);
    previews[1] = new ShapePreview(shape, 12, -4);
    previews[2] = new ShapePreview(shape, 20, 3);
    previews[3] = new ShapePreview(shape, 30, -2);
    previews[4] = new ShapePreview(shape, 50, 1);

    for (int i = 0; i < previews.length; i++) {
      shape.addObserver(previews[i]);
    }

    // LAYOUT
    javax.swing.JPanel graphicPanel, previewPanel, buttonPanel, drawingPanel;
    int borderWidth = 10;

    // preview panel
    // includes the three preview panes
    previewPanel = new javax.swing.JPanel();
    previewPanel.setLayout
        (new javax.swing.BoxLayout
            (previewPanel, javax.swing.BoxLayout.X_AXIS));
    for (int i = 0; i < previews.length; i++) {
      previewPanel.add(previews[i]);
    }

    // graphic panel
    // includes the shape view and the preview panel
    graphicPanel = new javax.swing.JPanel();
    graphicPanel.setLayout
        (new javax.swing.BoxLayout
            (graphicPanel, javax.swing.BoxLayout.Y_AXIS));
    graphicPanel.add(shapeView);
    graphicPanel.add(previewPanel);

    // drawing panel
    // includes the main tool bar and the graphic panel
    drawingPanel = new javax.swing.JPanel();
    drawingPanel.setLayout
        (new javax.swing.BoxLayout
            (drawingPanel, javax.swing.BoxLayout.X_AXIS));
    drawingPanel.add
        (javax.swing.Box.createHorizontalStrut(borderWidth));
    drawingPanel.add(leftPanel);
    drawingPanel.add(javax.swing.Box.createHorizontalStrut(15));
    drawingPanel.add(graphicPanel);
    drawingPanel.add(javax.swing.Box.createHorizontalStrut(15));
    drawingPanel.add(rightPanel);
    drawingPanel.add
        (javax.swing.Box.createHorizontalStrut(borderWidth));

    // button panel
    // includes the cancel, done
    buttonPanel = new org.nlogo.swing.ButtonPanel
        (new javax.swing.JButton[]{done, cancel});

    // name panel
    javax.swing.JLabel nameLabel = new javax.swing.JLabel("Name");
    nameText = new org.nlogo.swing.TextField(4);
    javax.swing.JPanel namePanel = new javax.swing.JPanel();
    namePanel.setLayout
        (new javax.swing.BoxLayout
            (namePanel, javax.swing.BoxLayout.X_AXIS));
    namePanel.add
        (javax.swing.Box.createHorizontalStrut(borderWidth));
    namePanel.add(javax.swing.Box.createHorizontalStrut(5));
    namePanel.add(nameLabel);
    namePanel.add(javax.swing.Box.createHorizontalStrut(5));
    namePanel.add(nameText);
    namePanel.add
        (javax.swing.Box.createHorizontalStrut(borderWidth));

    // Add everything to the content pane
    getContentPane()
        .setLayout(new javax.swing.BoxLayout
            (getContentPane(), javax.swing.BoxLayout.Y_AXIS));
    getContentPane().add
        (javax.swing.Box.createVerticalStrut(borderWidth));
    getContentPane().add(namePanel);
    getContentPane().add(javax.swing.Box.createVerticalStrut(15));
    getContentPane().add(drawingPanel);
    getContentPane().add(javax.swing.Box.createVerticalStrut(15));
    getContentPane().add(buttonPanel);
    getContentPane().add
        (javax.swing.Box.createVerticalStrut(borderWidth));

    nameText.setText(shape.name());
    shapeRotatable = shape.isRotatable();
    rotatableButton.setSelected(shapeRotatable);
    for (int i = 0; i < previews.length; i++) {
      previews[i].updateRotation(shapeRotatable);
    }
    colorSelection.setSelectedIndex(shape.getEditableColorIndex());
    elementColor = getColor(shape.getEditableColorIndex());
    setLocation(x + 10, y + 10);

    setTitle("Shape");
    name_$eqEditable(nameEditable);

    pack();
    getRootPane().setDefaultButton(done);
    nameText.requestFocus();
    setEditingElements(true);
    setVisible(true);
    shape.changed();

  } // end of constructor

  ///

  Class<? extends Element> getElementType() {
    return elementType;
  }

  java.awt.Color getElementColor() {
    return elementColor;
  }

  boolean fillShapes() {
    return fillShapes;
  }

  boolean isRotatable() {
    return shapeRotatable;
  }

  boolean snapToGrid() {
    return snapToGrid;
  }

  boolean editingElements() {
    return editingElements;
  }

  void setEditingElements(boolean e) {
    editingElements = e;
    editElements.setSelected(e);
    if (e) {
      shapeView.setCursor
          (java.awt.Cursor.getPredefinedCursor
              (java.awt.Cursor.DEFAULT_CURSOR));
    } else {
      shapeView.setCursor
          (java.awt.Cursor.getPredefinedCursor
              (java.awt.Cursor.CROSSHAIR_CURSOR));
    }

  }

  public void makeUndoableModification(Element el, int z) {
    undoableEdit = new UndoableModification(el, z);
    undoButton.setEnabled(undoableEdit.canUndo());
  }

  public void makeUndoableDraw(Element el) {
    undoableEdit = new UndoableDraw(el);
    undoButton.setEnabled(undoableEdit.canUndo());
  }

  public void makeUndoableUnfinishedPolygon() {
    undoableEdit = new UndoableUnfinishedPolygon();
    undoButton.setEnabled(undoableEdit.canUndo());
  }

  // Handle changes to current model
  public void update(Observable o, Object obj) {
    //shapeChanged = true ;
    deleteSelected.setEnabled(shapeView.hasSelectedElement());
    duplicateSelected.setEnabled(shapeView.hasSelectedElement());
    bringToFront.setEnabled(shapeView.hasSelectedElement());
    sendToBack.setEnabled(shapeView.hasSelectedElement());
  }

  // Attempts to save the current shape being drawn, prompting the
  // user if any issues come up
  private void saveShape() {
    String name;
    int overwrite;
    VectorShape newShape;

    // Make sure the shape has a name
    if (nameText.getText().equals("")) {
      name =
          javax.swing.JOptionPane.showInputDialog
              (this, "Name:", "Name Shape", javax.swing.JOptionPane.PLAIN_MESSAGE);
      if (name == null) {
        return;
      }
    } else {
      name = nameText.getText();
    }

    name = name.toLowerCase();

    // If the user chose to quit, don't save
    if (name == null || name.equals("")) {
      return;
    }

    String originalName = originalShape.name();
    // If this is an attempt to overwrite a shape, prompt for
    // permission to do it
    if (container.exists(name) && !name.equals(originalName)) {
      overwrite = javax.swing.JOptionPane.showConfirmDialog
          (this, "A shape with this name already exists. Do you want to replace it?",
              "Confirm Overwrite", javax.swing.JOptionPane.YES_NO_OPTION);
      if (overwrite != javax.swing.JOptionPane.YES_OPTION) {
        return;
      }
    }

    newShape = shape;
    newShape.name_$eq(name);
    newShape.setRotatable(shapeRotatable);
    newShape.markRecolorableElements(getColor(shape.getEditableColorIndex()),
        shape.getEditableColorIndex());

    container.update(originalShape, newShape);
    dispose();
  }

  private VectorShape getCurrentShape() {
    VectorShape currentShape = shape.clone();
    currentShape.name_$eq(nameText.getText());
    currentShape.setRotatable(shapeRotatable);
    currentShape.markRecolorableElements(getColor(shape.getEditableColorIndex()),
        shape.getEditableColorIndex());
    return currentShape;
  }

  // Sets <editableColor> to whatever the current selection in
  // <colorSelection> is
  private void setEditableColor() {
    shape.setEditableColorIndex(colorSelection.getSelectedIndex());
    shape.markRecolorableElements(getColor(shape.getEditableColorIndex()),
        shape.getEditableColorIndex());

  }

  private javax.swing.AbstractButton addToolBarButton
      (javax.swing.JToolBar toolbar,
       javax.swing.ButtonGroup group,
       javax.swing.Action action) {
    javax.swing.JToggleButton newButton =
        new javax.swing.JToggleButton(action) {
          @Override
          public java.awt.Insets getInsets() {
            // this is very ad hoc... - ST 10/4/05
            return new java.awt.Insets
                (3, 3, 3, 3);
          }
        };
    newButton.setText(null);
    toolbar.add(newButton);
    group.add(newButton);
    return newButton;
  }

  static java.awt.Color getColor(int index) {
    return new java.awt.Color
        (org.nlogo.api.Color.getARGBByIndex(index));
  }

  // Makes the name (un)editable.  Name should be uneditable for default shape
  private void name_$eqEditable(boolean editable) {
    nameText.setEnabled(editable);
  }

  private strictfp class ColorAction
      extends javax.swing.AbstractAction {
    private final java.awt.Color color;

    ColorAction(String name, java.awt.Color color, String toolTip) {
      super(name);
      this.color = color;
      putValue(SMALL_ICON,
          new org.nlogo.swing.ColorIcon(color, 13, 13));
      if (toolTip != null) {
        putValue(SHORT_DESCRIPTION, toolTip);
      }
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      elementColor = color;

      if (editingElements && (shapeView.getSelectedElement() != null)) {
        makeUndoableModification
            (shapeView.getSelectedElement(),
                shape.getElements()
                    .indexOf(shapeView.getSelectedElement()));
        shapeView.getSelectedElement().awtColor_$eq(color);
        shapeView.repaint();
      }
    }
  }

  private strictfp class CreateAction
      extends javax.swing.AbstractAction {
    private final Class<? extends Element> typeID;
    private final boolean filled;

    CreateAction(String name, Class<? extends Element> typeID, String toolTip, boolean filled) {
      super(name);
      this.typeID = typeID;
      this.filled = filled;
      putValue(SMALL_ICON,
          new javax.swing.ImageIcon
              (CreateAction.class.getResource
                  ("/images/shapes-editor/" + name + ".gif")));
      if (toolTip != null) {
        putValue(SHORT_DESCRIPTION, toolTip);
      }
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      if (shapeView.drawingPolygon()) {
        shapeView.selfFinishPolygon(true);
      }
      elementType = typeID;
      fillShapes = filled;
      editingElements = false;
      shapeView.deselectAll();
      shapeView.setCursor
          (java.awt.Cursor.getPredefinedCursor
              (java.awt.Cursor.CROSSHAIR_CURSOR));
    }
  }

  // this is used when the user modifies an existing shape, such as
  // by moving it or dragging a handle
  private strictfp class UndoableModification
      extends javax.swing.undo.AbstractUndoableEdit {
    private final Element originalElement;
    private final Element modifiedElement;
    private final int zOrder;

    public UndoableModification(Element el, int zOrder) {
      originalElement = (Element) el.clone();
      modifiedElement = el;
      this.zOrder = zOrder;
    }

    @Override
    public void undo() {
      super.undo();
      shape.remove(modifiedElement);
      shape.addAtPosition(zOrder, originalElement);
      if (shapeView.hasSelectedElement()) {
        shapeView.selectElement(originalElement);
      }
    }
  }

  // this is used when the user creates a new element
  private strictfp class UndoableDraw
      extends javax.swing.undo.AbstractUndoableEdit {
    private final Element newElement;

    public UndoableDraw(Element newElement) {
      this.newElement = newElement;
    }

    @Override
    public void undo() {
      super.undo();
      shape.remove(newElement);
    }
  }

  // this is used when the user deletes an element
  private strictfp class UndoableDeleteEdit
      extends javax.swing.undo.AbstractUndoableEdit {
    private final Element deletedElement;
    private final int zOrder;

    public UndoableDeleteEdit(Element el, int zOrder) {
      // is the call to clone() here really necessary?
      // not clear to me - ST 6/14/04, 7/31/04
      deletedElement = (Element) el.clone();
      this.zOrder = zOrder;
    }

    @Override
    public void undo() {
      super.undo();
      shape.addAtPosition(zOrder, deletedElement);
    }
  }

  // this is used when the user is in the middle of drawing a polygon;
  // if they press undo at that time, we abort the creation of that polygon.
  private strictfp class UndoableUnfinishedPolygon
      extends javax.swing.undo.AbstractUndoableEdit {
    @Override
    public void undo() {
      super.undo();
      shapeView.setTempElement(null);
    }
  }

  public interface VectorShapeContainer {
    boolean exists(String name);

    void update(Shape original, Shape newShape);
  }
}
