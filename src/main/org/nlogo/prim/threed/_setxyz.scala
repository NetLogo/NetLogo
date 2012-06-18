// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.agent.{ Observer3D, Turtle3D }
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context, EngineException }

class _setxyz extends Command {
  override def syntax = Syntax.commandSyntax(
    Array(Syntax.NumberType,
          Syntax.NumberType,
          Syntax.NumberType),
    "OT--", true)
  override def perform(context: Context) {
    context.agent match {
      case turtle: Turtle3D =>
        var newx = argEvalDouble(context, 0)
        var newy = argEvalDouble(context, 1)
        var newz = argEvalDouble(context, 2)
        try {
          val xvalue = newx.doubleValue
          val yvalue = newy.doubleValue
          val zvalue = newz.doubleValue
          val x = turtle.shortestPathX(xvalue)
          val y = turtle.shortestPathY(yvalue)
          val z = turtle.shortestPathZ(zvalue)
          if (x != xvalue)
            newx = Double.box(x)
          if (y != yvalue)
            newy = Double.box(y)
          if (z != zvalue)
            newz = Double.box(z)
          turtle.xyandzcor(newx, newy, newz)
        }
        catch {
          case _: org.nlogo.api.AgentException =>
            throw new EngineException(
              context, this,
              "The point [ "
              + newx.doubleValue + " , "
              + newy.doubleValue + " , "
              + newz.doubleValue + " ] "
              + "is outside of the boundaries of the world "
              + "and wrapping is not permitted in one or both directions.")
        }
      case observer: Observer3D =>
        val xcor = argEvalDoubleValue(context, 0)
        val ycor = argEvalDoubleValue(context, 1)
        val zcor = argEvalDoubleValue(context, 2)
        val rotationPoint = observer.rotationPoint
        observer.oxyandzcor(xcor, ycor, zcor)
        observer.face(rotationPoint.x, rotationPoint.y, rotationPoint.z)
    }
    context.ip = next
  }
}
