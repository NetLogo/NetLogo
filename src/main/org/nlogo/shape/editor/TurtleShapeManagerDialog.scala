// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import org.nlogo.shape.{VectorShape, ShapeChangeListener}
import org.nlogo.api.ShapeList

class TurtleShapeManagerDialog(parentFrame: java.awt.Frame,
                               world: org.nlogo.api.World,
                               shapeChangeListener: ShapeChangeListener,
                               modelReader: org.nlogo.shape.ModelSectionReader)
        extends ManagerDialog(parentFrame, modelReader, new DrawableList(world.turtleShapeList, shapeChangeListener, 10, 34))
                with org.nlogo.shape.TurtleShapesManagerInterface {

  shapesList.addListSelectionListener(this)

  // Load a new shapes editor to let the user create a new shape
  override def newShape() {
    new EditorDialog(shapesList, new VectorShape(), getLocation.x, getLocation.y, true)
  }

  // Edit an existing shape
  override def editShape() {
    val shape = shapesList.getOneSelected.asInstanceOf[VectorShape]
    if (shape != null) {
      new EditorDialog(shapesList, shape, getLocation.x, getLocation.y, !ShapeList.isDefaultShapeName(shape.getName))
    }
  }

  // Duplicate a shape, which can then be edited
  override def duplicateShape() {
    val shape = shapesList.getOneSelected.asInstanceOf[VectorShape]
    // You can only duplicate one shape at a time
    if ( shape != null )   {
      val newShape = shape.clone.asInstanceOf[VectorShape]
      newShape.setName("")
      new EditorDialog(shapesList, newShape, getLocation.x, getLocation.y, true )
    }
  }
}
