// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context, EngineException }
import org.nlogo.shape.InvalidShapeDescriptionException
import org.nlogo.window.GUIWorkspace

class _load3Dshapes extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.StringType), "O---", true)
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
      case _ =>
        // ok to just ignore, I guess - ST 5/17/11
    }
    context.ip = next
  }
}
