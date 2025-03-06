// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import org.nlogo.core.{ I18N, Shape, ShapeList, ShapeListTracker }
import org.nlogo.swing.OptionPane

import scala.reflect.ClassTag

class DrawableList[A <: Shape](shapeTracker: ShapeListTracker, rows: Int, height: Int, parent: java.awt.Component)(implicit ct: ClassTag[A])
  extends javax.swing.JList[A]
  with EditorDialog.VectorShapeContainer {

  def shapeList = shapeTracker.shapeList

  val listModel = new javax.swing.DefaultListModel[A]()

  setVisibleRowCount(rows)
  setModel(listModel)
  setFixedCellHeight(height)
  setCellRenderer(new ShapeCellRenderer())

  //  Make sure the list of available shapes is up to date, filtering by name if provided
  def update(name: Option[String] = None): Unit = {
    listModel.clear()

    name match {
      case Some(str) =>
        shapeList.shapes.foreach {
          case s: A if s.name.toLowerCase.contains(str.toLowerCase) => listModel.addElement(s)
          case _ =>
        }

      case _ =>
        shapeList.shapes.foreach {
          case s: A => listModel.addElement(s)
          case _ =>
        }
    }
  }

  override def update(originalShape: Shape, newShape: Shape): Unit = {
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
      if (listModel.getElementAt(index).name == name)
        namedIndex = index
      index += 1
    }
    addSelectionInterval(namedIndex, namedIndex) // Select that index
    ensureIndexIsVisible(namedIndex)
  }

  def elementAt(index: Int): A =
    listModel.getElementAt(index)

  def getOneSelected: Option[A] = {
    val selected: Array[Int] = getSelectedIndices
    if (selected.length == 1)
      Option(listModel.get(selected(0)))
    else
      None
  }

  def getShape(index: Int): Option[A] =
    Option(listModel.get(index))

  def getShapeNames: Set[String] =
    shapeList.names

  def exists(name: String): Boolean =
    shapeList.exists(name)

  // Select the shape with a given index
  def selectShapeIndex(index: Int): Unit = {
    if (index < listModel.size) {
      addSelectionInterval(index, index)
      ensureIndexIsVisible(index)
    }
  }

  private def confirmDeletion(deleteCount: Int): Boolean = {
    val message =
      if (deleteCount > 1)
        Some(I18N.gui.getN("tools.shapesEditor.delete.many.confirm", Int.box(deleteCount)))
      else if (deleteCount == 1)
        Some(I18N.gui.get("tools.shapesEditor.delete.one.confirm"))
      else
        None

    message.map { msg =>
      new OptionPane(parent, I18N.gui.get("tools.shapesEditor.delete"), msg, OptionPane.Options.YesNo,
                     OptionPane.Icons.Question).getSelectedIndex == 0
    }.getOrElse(false)
  }

  // Delete a shape of the current model
  def deleteShapes(): Seq[Shape] = {
    val selected: Array[Int] = getSelectedIndices

    if (confirmDeletion(selected.size)) {
      // Remove the selected shapes from the model
      val shapesToDelete = selected
        .map(i => (i, listModel.get(i)))
        .filter(_._2.name != ShapeList.DefaultShapeName)

      // we have to reverse here to delete indices in
      // decreasing order
      shapesToDelete.reverse.foreach {
        case (i, shape) =>
          removeShape(shape)
          listModel.remove(i)
      }

      val newSelectedShapeIndex =
        if (selected(0) >= listModel.size)
          selected(0) - 1
        else
          selected(0)

      selectShapeIndex(newSelectedShapeIndex)
      shapesToDelete.collect {
        case (_, s: A) => s
      }.toSeq
    } else {
      Seq()
    }
  }

  def addShape(shape: Shape): Unit = {
    Option(shape).foreach(shapeTracker.add)
  }

  def removeShape(shape: Shape): Unit = {
    Option(shape).foreach(shapeTracker.removeShape)
  }
}
