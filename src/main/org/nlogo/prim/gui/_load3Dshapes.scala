package org.nlogo.prim.gui

import org.nlogo.nvm.{ Command, Context, EngineException, Syntax }
import org.nlogo.shape.InvalidShapeDescriptionException
import org.nlogo.window.GUIWorkspace

class _load3Dshapes extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.TYPE_STRING), "O---", true)
  override def perform(context: Context) {
    val filename = argEvalString(context, 0)
    workspace match {
      case gw: GUIWorkspace =>
        try gw.addCustomShapes(filename)
        catch {
          case e: java.io.IOException =>
            throw new EngineException(context, this, e.getMessage)
          case e: InvalidShapeDescriptionException =>
            throw new EngineException(context, this, "Invalid shape file")
        }
    }
    context.ip = next
  }
}
