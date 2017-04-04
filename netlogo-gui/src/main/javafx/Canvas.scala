// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.javafx

import javafx.event.EventHandler
import javafx.scene.canvas.{ Canvas => JFXCanvas }
import javafx.scene.input.MouseEvent

import org.nlogo.core.View
import org.nlogo.internalapi.WritableGUIWorkspace

class Canvas(val view: View)
  extends JFXCanvas(view.dimensions.width * view.dimensions.patchSize, view.dimensions.height * view.dimensions.patchSize) {

  def attachToWorkspace(workspace: WritableGUIWorkspace): Unit = {
    addEventFilter(MouseEvent.ANY, new ViewMouseHandler(view, this, workspace))
  }
}

class ViewMouseHandler(view: View, canvas: Canvas, workspace: WritableGUIWorkspace) extends EventHandler[MouseEvent] {
  import view.dimensions

  // TODO: This doesn't handle altered world perspectives
  def handle(e: MouseEvent): Unit = {
    e.getEventType match {
      case MouseEvent.MOUSE_PRESSED  => workspace.setMouseDown(true)
      case MouseEvent.MOUSE_RELEASED => workspace.setMouseDown(false)
      case MouseEvent.MOUSE_ENTERED  => workspace.setMouseInside(true)
      case MouseEvent.MOUSE_EXITED   => workspace.setMouseInside(false)
      case _ =>
        def translateCoordinateX(mouseCor: Double, displayedDimSize: Double, worldDimSize: Int, min: Int, max: Int): Double = {
          val translated = ((mouseCor / displayedDimSize) * worldDimSize) + (min - 0.5)
          if (translated < min - 0.5)          min - 0.5
          else if (translated >= max + 0.5)    max + 0.4999999
          else translated
        }

        def translateCoordinateY(mouseCor: Double, displayedDimSize: Double, worldDimSize: Int, min: Int, max: Int): Double = {
          val translated = max + 0.4999999 - (worldDimSize * (mouseCor / displayedDimSize))
          if (translated < min - 0.5)          min - 0.5
          else if (translated >= max + 0.5)    max + 0.4999999
          else translated
        }

        val x =
          translateCoordinateX(e.getX, canvas.getWidth, dimensions.width, dimensions.minPxcor, dimensions.maxPxcor)

        val y =
          translateCoordinateY(e.getY, canvas.getHeight, dimensions.height, dimensions.minPycor, dimensions.maxPycor)

        // need to translate these to coordinate space
        workspace.setMouseCors(x, y)
    }
  }
}
