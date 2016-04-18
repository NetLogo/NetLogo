// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import org.nlogo.api.ModelLoader
import org.nlogo.core.{ AgentKind, Model, Shape, ShapeListTracker }, Shape.{ LinkShape => CoreLinkShape }
import org.nlogo.shape.ShapeConverter
import org.nlogo.shape.LinkShape

class LinkShapeManagerDialog(parentFrame: java.awt.Frame,
                             world: org.nlogo.api.World,
                             modelLoader: ModelLoader)
        extends ManagerDialog[LinkShape](parentFrame, modelLoader, world.linkShapes)
                with org.nlogo.shape.LinkShapesManagerInterface {

  libraryLabel.setVisible(false)
  shapesList.addListSelectionListener(this)

  override def shapeKind: AgentKind = AgentKind.Link

  override def modelShapes(m: Model): Seq[Shape] = m.linkShapes

  def displayableShapeFromCoreShape(shape: Shape): Option[LinkShape] = {
    shape match {
      case l: CoreLinkShape => Some(ShapeConverter.baseLinkShapeToLinkShape(l))
      case _ => None
    }
  }

  // Load a new shapes editor to let the user create a new shape
  override def newShape(): Unit = {
    new LinkEditorDialog(shapesList, new LinkShape(), getLocation.x, getLocation.y)
  }

  // Edit an existing shape
  override def editShape(): Unit = {
    shapesList.getOneSelected.foreach { shape =>
      new LinkEditorDialog(shapesList, shape, getLocation.x, getLocation.y)
    }
  }

  // Duplicate a shape, which can then be edited
  override def duplicateShape(): Unit = {
    shapesList.getOneSelected.foreach { shape =>
      // You can only duplicate one shape at a time
      val newShape = shape.clone.asInstanceOf[LinkShape]
      newShape.name_$eq("")
      new LinkEditorDialog(shapesList, newShape, getLocation.x, getLocation.y)
    }
  }
}
