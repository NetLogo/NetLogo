// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context, EngineException }
import org.nlogo.shape.InvalidShapeDescriptionException

class _load3Dshapes extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.StringType), "O---", true)
  override def perform(context: Context) {
    try workspace.addCustomShapes(argEvalString(context, 0))
    catch {
      case e: java.io.IOException =>
        throw new EngineException(context, this, e.getMessage)
    }
    context.ip = next
  }
}
