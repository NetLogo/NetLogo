// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import java.awt.Frame

import org.nlogo.api.{ AbstractModelLoader, World }
import org.nlogo.shape.{ ShapeConverter, VectorShape },
  ShapeConverter.baseVectorShapeToVectorShape
import org.nlogo.core.{ AgentKind, Model, Shape, ShapeList },
  Shape.{ VectorShape => CoreVectorShape },
  ShapeList.isDefaultShapeName

class TurtleShapeManagerDialog(parentFrame: Frame,
                               world: World,
                               modelLoader: AbstractModelLoader)
        extends ManagerDialog[VectorShape](parentFrame, modelLoader, world.turtleShapes)
                with org.nlogo.shape.TurtleShapesManagerInterface {

  shapesList.addListSelectionListener(this)

  override def shapeKind = AgentKind.Turtle

  override def modelShapes(m: Model): Seq[Shape] = m.turtleShapes

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
}
