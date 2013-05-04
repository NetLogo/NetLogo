// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Syntax, AgentException, AgentKind }
import org.nlogo.nvm.{ Reporter, Context }
import org.nlogo.agent.{ AgentSet, AgentSetBuilder }

class _breedat(breedName: String) extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.NumberType, Syntax.NumberType),
      Syntax.TurtlesetType,
      "-TP-")

  override def toString =
    super.toString + ":" + breedName

  override def report(context: Context): AgentSet = {
    val dx = argEvalDoubleValue(context, 0)
    val dy = argEvalDoubleValue(context, 1)
    val patch =
      try context.agent.getPatchAtOffsets(dx, dy)
      catch { case _: AgentException =>
        return world.noTurtles }
    if (patch == null)
      world.noTurtles
    else {
      val builder = new AgentSetBuilder(AgentKind.Turtle, patch.turtleCount)
      val breed = world.getBreed(breedName)
      val iter = patch.turtlesHere.iterator
      while(iter.hasNext) {
        val turtle = iter.next()
        if (turtle != null && (turtle.getBreed eq breed))
          builder.add(turtle)
      }
      builder.build()
    }
  }

}
