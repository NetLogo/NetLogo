// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import org.nlogo.core.{ Shape, ShapeList }
import org.nlogo.shape.ShapeChangeListener

import java.util.{ ArrayList => JArrayList, List => JList }

class DrawableList(val shapeList: ShapeList, private val shapeChangeListener: ShapeChangeListener, rows: Int, height: Int)
  extends javax.swing.JList[String]
  with EditorDialog.VectorShapeContainer {

  val listModel = new javax.swing.DefaultListModel[String]()
  var shapes = new JArrayList[Shape]()
  private var dlParent: java.awt.Component = _

  setVisibleRowCount(rows)
  setModel(listModel)
  setFixedCellHeight(height)

  def setParent(parent: java.awt.Component): Unit = {
    this.dlParent = parent
  }

  //  Make sure the list of available shapes is up to date
  def update(): Unit = {
    listModel.clear()
    shapes.clear()
    shapeList.shapes.foreach { s =>
      shapes.add(s)
      listModel.addElement(s.name)
    }
  }

  def update(originalShape: Shape, newShape: Shape): Unit = {
    // If you changed the name of the shape, get rid of the shape
    // with the old name
    if (originalShape.name != newShape.name && ShapeList.DefaultShapeName != originalShape.name)
      removeShape(originalShape)

    addShape(newShape)

    update() // Update the shapes manager's list
    selectShapeName(newShape.name)
  }

  // Select a shape in the list
  def selectShapeName(name: String): Unit = {
    var index = 0
    var namedIndex = -1
    // Iterate through items to find index of <name>
    while (index < listModel.size && namedIndex == -1) {
      if (listModel.elementAt(index) == name)
        namedIndex = index
      index += 1
    }
    addSelectionInterval(namedIndex, namedIndex)    // Select that index
    ensureIndexIsVisible(namedIndex)
  }

  def elementAt(index: Int): AnyRef =
    listModel.elementAt(index)

  def getOneSelected: Shape = {
    val selected: Array[Int] = getSelectedIndices
    if (selected.length == 1) {
      shapes.get(selected(0))
    } else {
      null
    }
  }

  def getShape(index: Int): Shape =
    shapes.get(index)

  def getShapeNames: Set[String] =
    shapeList.names

  def exists(name: String): Boolean =
    return shapeList.exists(name);

  // Select the shape with a given index
  def selectShapeIndex(index: Int): Unit = {
    addSelectionInterval(index, index)
    ensureIndexIsVisible(index)
  }

  // Delete a shape of the current model
  def deleteShapes(): JList[Shape] = {
    val selected: Array[Int] = getSelectedIndices
    var deletedShapes: JList[Shape] = new JArrayList[Shape]() // maybe could be val?

    // Confirm that the user wants to delete
    val delete =
      if (selected.length > 1) {
        javax.swing.JOptionPane.showConfirmDialog(dlParent, "Are you sure you want to delete these "
          + selected.length + " shapes?", "Delete", javax.swing.JOptionPane.YES_NO_OPTION)
      } else if (selected.length == 1) {
        javax.swing.JOptionPane.showConfirmDialog(dlParent, "Are you sure you want to delete this shape?",
          "Delete", javax.swing.JOptionPane.YES_NO_OPTION)
      } else {
        javax.swing.JOptionPane.NO_OPTION
      }

    if (delete == javax.swing.JOptionPane.YES_OPTION) {
      // Remove the selected shapes from the model
      val shapesToDelete = selected
        .map(i => (i, shapes.get(i)))
        .filter(_._2.name != ShapeList.DefaultShapeName)

      // we have to reverse here to delete indices in
      // decreasing order
      shapesToDelete.reverse.foreach {
        case (i, shape) =>
          deletedShapes.add(shape)
          removeShape(shape)
          shapes.remove(i)
          listModel.remove(i)
      }

      val newSelectedShapeIndex =
        if (selected(0) >= shapes.size)
          selected(0) - 1
        else
          selected(0)

      selectShapeIndex(newSelectedShapeIndex)
    }

    deletedShapes
  }

  def addShape(shape: Shape): Unit = {
    Option(shape).foreach { s =>
      val replacedShape = shapeList.add(s)
      if (shapeChangeListener != null) {
        val changedShape = if (replacedShape == null) s else replacedShape
        shapeChangeListener.shapeChanged(changedShape)
      }
    }
  }

  def removeShape(shape: Shape): Unit = {
    val removedShape = shapeList.removeShape(shape)
    if (removedShape != null && shapeChangeListener != null) {
      shapeChangeListener.shapeRemoved(shape)
      shapeChangeListener.shapeChanged(removedShape)
    }
  }
}
