// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import java.awt.Frame

import org.nlogo.analytics.Analytics
import org.nlogo.api.{ AbstractModelLoader, World }
import org.nlogo.core.{ AgentKind, I18N, Model, Shape, ShapeList },
  Shape.{ VectorShape => CoreVectorShape }, ShapeList.isDefaultShapeName
import org.nlogo.shape.{ ShapeConverter, VectorShape },
  ShapeConverter.baseVectorShapeToVectorShape
import org.nlogo.swing.{ Button, DialogButton }

class TurtleShapeManagerDialog(parentFrame: Frame,
                               world: World,
                               modelLoader: AbstractModelLoader)
        extends ManagerDialog[VectorShape](parentFrame, modelLoader, world.turtleShapes)
                with org.nlogo.shape.TurtleShapesManagerInterface {

  shapesList.addListSelectionListener(this)

  override def shapeKind = AgentKind.Turtle

  override def modelShapes(m: Model): Seq[Shape] = m.turtleShapes

  override def additionalButton: Option[Button] =
    Some(new DialogButton(false, I18N.gui.get("tools.shapesEditor.importFromLibrary"), () => importFromLibrary()))

  def displayableShapeFromCoreShape(shape: Shape): Option[VectorShape] = {
    shape match {
      case v: CoreVectorShape => Some(baseVectorShapeToVectorShape(v))
      case _ => None
    }
  }

  // Load a new shapes editor to let the user create a new shape
  override def newShape(): Unit = {
    new EditorDialog(this, shapesList, new VectorShape(), true)
  }

  // Edit an existing shape
  override def editShape(): Unit = {
    shapesList.getOneSelected.foreach { shape =>
      new EditorDialog(this, shapesList, shape, !isDefaultShapeName(shape.name))
    }
  }

  // Duplicate a shape, which can then be edited
  override def duplicateShape(): Unit = {
    shapesList.getOneSelected.foreach { (shape: VectorShape) =>
      val newShape = shape.clone
      newShape.name = ""
      new EditorDialog(this, shapesList, newShape, true)
    }
  }

  private def importFromLibrary(): Unit = {
    importDialog = Some(new ImportDialog(this, this, drawableListFromModelShapes(
      (Model.defaultTurtleShapes ++ Model.libraryTurtleShapes).map(baseVectorShapeToVectorShape))))
  }

  override def setVisible(visible: Boolean): Unit = {
    if (visible)
      Analytics.turtleShapeEditorOpen()

    super.setVisible(visible)
  }
}
