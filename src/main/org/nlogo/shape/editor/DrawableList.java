// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor;

import org.nlogo.api.Shape;
import org.nlogo.api.ShapeList;
import org.nlogo.shape.ShapeChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public strictfp class DrawableList
    extends javax.swing.JList<String>
    implements EditorDialog.VectorShapeContainer {
  javax.swing.DefaultListModel<String> listModel;
  final ShapeList shapeList;
  List<Shape> shapes;
  private final ShapeChangeListener shapeChangeListener;
  private java.awt.Component parent;

  public DrawableList(ShapeList shapeList, ShapeChangeListener listener,
               int rows, int height) {
    this.shapeChangeListener = listener;
    this.shapeList = shapeList;
    putClientProperty("Quaqua.List.style", "striped");
    setVisibleRowCount(rows);
    listModel = new javax.swing.DefaultListModel<String>();
    setModel(listModel);
    setFixedCellHeight(height);
  }

  public void setParent(java.awt.Component parent) {
    this.parent = parent;
  }

  //  Make sure the list of available shapes is up to date
  public void update() {
    listModel.clear();
    shapes = shapeList.getShapes();
    for (Shape shape : shapes) {
      listModel.addElement(shape.getName());
    }
  }

  public void update(Shape originalShape, Shape newShape) {
    // If you changed the name of the shape, get rid of the shape
    // with the old name
    if ((!originalShape.getName().equals(newShape.getName())) &&
        (!ShapeList.isDefaultShapeName(originalShape.getName()))) {
      removeShape(originalShape);
    }

    addShape(newShape);

    update(); // Update the shapes manager's list
    selectShapeName(newShape.getName());
  }

  // Select a shape in the list
  public void selectShapeName(String name) {
    int index = -1;
    while (++index < listModel.size())    // Iterate through all the items of the list until you have
    {                    //  the index of <name>
      if (listModel.elementAt(index).equals(name)) {
        break;
      }
    }
    addSelectionInterval(index, index);    // Select that index
    ensureIndexIsVisible(index);
  }

  public Object elementAt(int index) {
    return listModel.elementAt(index);
  }

  public Shape getOneSelected() {
    int[] selected = getSelectedIndices();
    if (selected.length == 1)   // You can only edit one shape at a time
    {
      return shapes.get(selected[0]);
    }

    return null;
  }

  public Shape getShape(int index) {
    return shapes.get(index);
  }

  public Set<String> getShapeNames() {
    return shapeList.getNames();
  }

  public boolean exists(String name) {
    return shapeList.exists(name);
  }

  // Select the shape with a given index
  void selectShapeIndex(int index) {
    addSelectionInterval(index, index);
    ensureIndexIsVisible(index);
  }

  // Delete a shape of the current model
  List<Shape> deleteShapes() {
    int[] selected = getSelectedIndices();
    int delete;
    List<Shape> deletedShapes = new ArrayList<Shape>();

    // Confirm that the user wants to delete
    if (selected.length > 1) {
      delete = javax.swing.JOptionPane.showConfirmDialog
          (parent, "Are you sure you want to delete these "
              + selected.length + " shapes?", "Delete", javax.swing.JOptionPane.YES_NO_OPTION);
    } else if (selected.length == 1) {
      delete = javax.swing.JOptionPane.showConfirmDialog(parent, "Are you sure you want to delete this shape?",
          "Delete", javax.swing.JOptionPane.YES_NO_OPTION);
    } else {
      return deletedShapes;
    }

    if (delete != javax.swing.JOptionPane.YES_OPTION) {
      return deletedShapes;
    }

    for (int i = 0; i < selected.length; ++i)      // Remove the selected shapes from the model
    {
      // Don't delete the default turtle
      if (!ShapeList.isDefaultShapeName(shapes.get(selected[i]).getName())) {
        Shape shape = shapes.get(selected[i]);
        deletedShapes.add(shape);
        removeShape(shape);
      }
    }

    for (int i = selected.length - 1; i >= 0; --i)    // Update the shapes manager to reflect those deletions
    {
      if (!(ShapeList.isDefaultShapeName(shapes.get(selected[i]).getName()))) {
        shapes.remove(selected[i]);
        listModel.remove(selected[i]);
      }
    }

    selectShapeIndex(((selected[0] >= shapes.size()) ? selected[0] - 1 : selected[0]));
    return deletedShapes;
  }

  void addShape(Shape shape) {
    if (shape != null) {
      Shape replacedShape = shapeList.add(shape);
      if (shapeChangeListener != null) {
        shapeChangeListener.shapeChanged
            (replacedShape == null ? shape : replacedShape);
      }
    }
  }

  void removeShape(Shape shape) {
    Shape removedShape = shapeList.removeShape(shape);
    if (removedShape != null && shapeChangeListener != null) {
      shapeChangeListener.shapeRemoved(shape);
      shapeChangeListener.shapeChanged(removedShape);
    }
  }

}
