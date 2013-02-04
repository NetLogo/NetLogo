// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.agent.{ Agent3D, AgentSet, ArrayAgentSet, Turtle }
import org.nlogo.api.{ AgentException, AgentKind, Syntax }
import org.nlogo.nvm.{ Context, Reporter }

class _turtlesat extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.NumberType,
                                Syntax.NumberType,
                                Syntax.NumberType),
                          Syntax.TurtlesetType, "-TP-")
  override def report(context: Context): AgentSet = {
    val dx = argEvalDoubleValue(context, 0)
    val dy = argEvalDoubleValue(context, 1)
    val dz = argEvalDoubleValue(context, 2)
    val patch =
      try context.agent.asInstanceOf[Agent3D].getPatchAtOffsets(dx, dy, dz)
      catch {
        case _: AgentException =>
          return world.noTurtles
      }
    if (patch == null)
      world.noTurtles
    else {
      val agentset = ArrayAgentSet.withCapacity(
        AgentKind.Turtle, patch.turtleCount)
      val it = patch.turtlesHere.iterator
      while(it.hasNext) {
        val turtle = it.next()
        if (turtle != null)
          agentset.add(turtle)
      }
      agentset
    }
  }
}
