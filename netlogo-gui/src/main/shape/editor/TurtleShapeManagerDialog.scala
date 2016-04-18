// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import org.nlogo.api.ModelLoader
import org.nlogo.swing.Implicits._
import org.nlogo.shape.VectorShape
import org.nlogo.core.{ AgentKind, I18N, Model, Shape, ShapeList }, Shape.{ VectorShape => CoreVectorShape }
import org.nlogo.core.ShapeParser.parseVectorShapes
import org.nlogo.shape.ShapeConverter
import java.awt.Component
import javax.swing.{ JButton, Box }

class TurtleShapeManagerDialog(parentFrame: java.awt.Frame,
                               world: org.nlogo.api.World,
                               modelLoader: ModelLoader)
        extends ManagerDialog[VectorShape](parentFrame, modelLoader, world.turtleShapes)
                with org.nlogo.shape.TurtleShapesManagerInterface {

  shapesList.addListSelectionListener(this)

  lazy val libraryButton =
    new JButton(I18N.gui("importFromLibrary")) {addActionListener(() => importFromLibrary())}

  override def shapeKind = AgentKind.Turtle

  override def modelShapes(m: Model): Seq[Shape] = m.turtleShapes

  override def importButtons: Seq[Component] =
    Seq(libraryButton, Box.createHorizontalStrut(5)) ++ super.importButtons

  def displayableShapeFromCoreShape(shape: Shape): Option[VectorShape] = {
    shape match {
      case v: CoreVectorShape => Some(ShapeConverter.baseVectorShapeToVectorShape(v))
      case _ => None
    }
  }

  // Load a new shapes editor to let the user create a new shape
  override def newShape(): Unit = {
    new EditorDialog(shapesList, new VectorShape(), getLocation.x, getLocation.y, true)
  }

  // Edit an existing shape
  override def editShape(): Unit = {
    shapesList.getOneSelected.foreach { shape =>
      new EditorDialog(shapesList, shape, getLocation.x, getLocation.y, !ShapeList.isDefaultShapeName(shape.name))
    }
  }

  // Duplicate a shape, which can then be edited
  override def duplicateShape(): Unit = {
    shapesList.getOneSelected.foreach { (shape: VectorShape) =>
      // You can only duplicate one shape at a time
      val newShape = shape.clone
      newShape.name_$eq("")
      new EditorDialog(shapesList, newShape, getLocation.x, getLocation.y, true)
    }
  }

  // Import shapes from shapes library
  private def importFromLibrary(): Unit = {
    val defaultShapes = org.nlogo.util.Utils.getResourceAsStringArray("/system/defaultShapes.txt")
    val libraryShapes = org.nlogo.util.Utils.getResourceAsStringArray("/system/libraryShapes.txt")
    val mergedShapes = defaultShapes.toList ::: ("" :: libraryShapes.toList)
    drawableListFromImportedShapes(mergedShapes.toArray) match {
      case Some(drawableList) =>
        importDialog = new ImportDialog(parentFrame, this, drawableList)
        shapesList.requestFocus()
      case None =>
        javax.swing.JOptionPane.showMessageDialog(this,
          I18N.gui.get("tools.shapesEditor.import.libraryError"),
          I18N.gui.get("tools.shapesEditor.import"),
          javax.swing.JOptionPane.WARNING_MESSAGE)
    }
  }

  private def drawableListFromImportedShapes(shapeStrings: Array[String]): Option[DrawableList[VectorShape]] = {
    try {
      val parsedShapes = parseVectorShapes(shapeStrings)
        .map(ShapeConverter.baseVectorShapeToVectorShape)
      Some(drawableListFromModelShapes(ShapeList.sortShapes(parsedShapes)))
    } catch {
      case e: IllegalArgumentException =>
        None
    }
  }
}
