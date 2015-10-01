// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ AgentSet, Turtle }
import org.nlogo.api.{ Nobody, Syntax }
import org.nlogo.nvm.{ Context, EngineException, Reporter }

class _breedsingular(breedName: String) extends Reporter {
  override def syntax: Syntax =
    Syntax.reporterSyntax(Array[Int](Syntax.NumberType), Syntax.TurtleType | Syntax.NobodyType)

  override def toString: String = s"${super.toString}:$breedName"

  override def report(context: Context) = report_1(context, argEvalDoubleValue(context, 0))

  def report_1(context: Context, idDouble: Double): AnyRef = {
    val id = validLong(idDouble)
    if (id != idDouble)
      throw new EngineException(context, this, s"$idDouble is not an integer")
    val turtle = world.getTurtle(id)
    if (turtle == null) return Nobody
    val breed = world.getBreed(breedName)
    if (!breed.contains(turtle))
      throw new EngineException(
        context, this, s"$turtle is not a ${world.getBreedSingular(breed)}")
    turtle
  }
}
