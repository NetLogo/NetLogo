// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.agent.Turtle3D
import org.nlogo.api.{ Syntax, Vect }
import org.nlogo.nvm.{ Command, Context }

class _tiltup extends Command {
  switches = true

  override def syntax =
    Syntax.commandSyntax(Array(Syntax.NumberType), "-T--")

  override def perform(context: Context) {
    val delta = argEvalDoubleValue(context, 0)
    val turtle = context.agent.asInstanceOf[Turtle3D]
    val v = Vect.toVectors(turtle.heading,
                           turtle.pitch,
                           turtle.roll)
    val pitch = new Vect(
      0, StrictMath.cos(StrictMath.toRadians(delta)),
      StrictMath.sin(StrictMath.toRadians(delta)))
    val orthogonal = v(1).cross(v(0))
    val forward = Vect.axisTransformation(pitch, v(1), v(0), orthogonal)
    val angles = Vect.toAngles(forward, v(1))
    turtle.headingPitchAndRoll(angles(0), angles(1), angles(2))
    context.ip = next
  }
}
