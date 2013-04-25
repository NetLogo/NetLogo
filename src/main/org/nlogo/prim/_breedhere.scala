// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, AgentKind }
import org.nlogo.nvm.{ Reporter, Context }
import org.nlogo.agent.{ AgentSet, AgentSetBuilder, Turtle, Patch }

class _breedhere(breedName: String) extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(Syntax.TurtlesetType, "-TP-")

  override def toString =
    super.toString + ":" + breedName

  override def report(context: Context) =
    report_1(context)

  def report_1(context: Context): AgentSet = {
    val patch = context.agent match {
      case t: Turtle =>
        t.getPatchHere
      case p: Patch =>
        p
    }
    val builder = new AgentSetBuilder(AgentKind.Turtle, patch.turtleCount)
    val breed = world.getBreed(breedName)
    val iter = patch.turtlesHere.iterator
    while(iter.hasNext) {
      val turtle = iter.next()
      if (turtle.getBreed eq breed)
        builder.add(turtle)
    }
    builder.build()
  }

}
