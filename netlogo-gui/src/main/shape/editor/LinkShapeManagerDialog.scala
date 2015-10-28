// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import org.nlogo.shape.{ ShapeConverter, ShapeChangeListener, LinkShape}

import org.nlogo.core.Shape
import org.nlogo.core.ShapeParser.parseLinkShapes

class LinkShapeManagerDialog(parentFrame: java.awt.Frame,
                             world: org.nlogo.api.World,
                             shapeChangeListener: ShapeChangeListener,
                             modelReader: org.nlogo.shape.ModelSectionReader)
        extends ManagerDialog(parentFrame, modelReader, new DrawableList(world.linkShapeList, shapeChangeListener, 10, 34))
                with org.nlogo.shape.LinkShapesManagerInterface {

  libraryButton.setVisible(false)
  libraryLabel.setVisible(false)
  shapesList.addListSelectionListener(this)

  // Load a new shapes editor to let the user create a new shape
  override def newShape() {
    new LinkEditorDialog(shapesList, new LinkShape(), getLocation.x, getLocation.y)
  }

  // Edit an existing shape
  override def editShape() {
    val shape = shapesList.getOneSelected.asInstanceOf[LinkShape]
    if (shape != null) new LinkEditorDialog(shapesList, shape, getLocation.x, getLocation.y)
  }

  // Duplicate a shape, which can then be edited
  override def duplicateShape() {
    val shape = shapesList.getOneSelected.asInstanceOf[LinkShape]
    // You can only duplicate one shape at a time
    if (shape != null) {
      val newShape = shape.clone.asInstanceOf[LinkShape]
      newShape.name_$eq("")
      new LinkEditorDialog(shapesList, newShape, getLocation.x, getLocation.y)
    }
  }

  override def parseShapes(shapes: Array[String], version: String): Seq[Shape] = {
    parseLinkShapes(shapes).map(ShapeConverter.baseLinkShapeToLinkShape)
  }
}
