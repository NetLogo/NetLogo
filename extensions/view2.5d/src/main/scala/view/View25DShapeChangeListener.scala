// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package view25d.view

import java.awt.GraphicsEnvironment
import java.util.Objects

import org.nlogo.api.AppInterface

import view25d.View25DExtension

object View25DShapeChangeListener {
  def listen(app: AppInterface): Unit = {
    app.workspace.world.turtleShapes.subscribe(_ => View25DExtension.updateTurtleShapesAllViews())
  }
}
