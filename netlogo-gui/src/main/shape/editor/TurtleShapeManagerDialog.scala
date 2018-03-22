// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import java.awt.{ Component, Frame }
import javax.swing.{ Box, JButton }

import org.nlogo.api.{ FileIO, ModelLoader, World }
import org.nlogo.swing.Implicits._
import org.nlogo.shape.{ ShapeConverter, VectorShape },
  ShapeConverter.baseVectorShapeToVectorShape
import org.nlogo.core.{ AgentKind, I18N, Model, Shape, ShapeList, ShapeParser },
  Shape.{ VectorShape => CoreVectorShape },
  ShapeList.{ isDefaultShapeName, sortShapes },
  ShapeParser.parseVectorShapes

object TurtleShapeManagerDialog {
  val DefaultShapePath = "/system/defaultShapes.txt"
  val LibraryShapePath = "/system/libraryShapes.txt"
}

import TurtleShapeManagerDialog._

class TurtleShapeManagerDialog(parentFrame: Frame,
                               world: World,
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
      case v: CoreVectorShape => Some(baseVectorShapeToVectorShape(v))
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
      new EditorDialog(shapesList, shape, getLocation.x, getLocation.y, !isDefaultShapeName(shape.name))
    }
  }

  // Duplicate a shape, which can then be edited
  override def duplicateShape(): Unit = {
    shapesList.getOneSelected.foreach { (shape: VectorShape) =>
      val newShape = shape.clone
      newShape.name = ""
      new EditorDialog(shapesList, newShape, getLocation.x, getLocation.y, true)
    }
  }

  // Import shapes from shapes library
  private def importFromLibrary(): Unit = {
    val defaultShapes = FileIO.getResourceAsStringArray(DefaultShapePath)
    val libraryShapes = FileIO.getResourceAsStringArray(LibraryShapePath)
    val mergedShapes = defaultShapes.toList ::: ("" :: libraryShapes.toList)
    drawableListFromImportedShapes(mergedShapes.toArray) match {
      case Some(drawableList) =>
        importDialog = Some(new ImportDialog(TurtleShapeManagerDialog.this, this, drawableList))
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
        .map(baseVectorShapeToVectorShape)
      Some(drawableListFromModelShapes(sortShapes(parsedShapes)))
    } catch {
      case e: IllegalArgumentException =>
        None
    }
  }
}
