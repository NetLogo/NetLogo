// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.agent.{ Agent3D, AgentSet, AgentSetBuilder }
import org.nlogo.api.AgentException
import org.nlogo.core.AgentKind
import org.nlogo.nvm.{ Context, Reporter }

class _turtlesat extends Reporter {

  override def report(context: Context): AgentSet = {
    val dx = argEvalDoubleValue(context, 0)
    val dy = argEvalDoubleValue(context, 1)
    val dz = argEvalDoubleValue(context, 2)
    val patch =
      try context.agent.asInstanceOf[Agent3D].getPatchAtOffsets(dx, dy, dz)
      catch {
        case _: AgentException => return AgentSet.emptyTurtleSet
      }
    if (patch == null)
      AgentSet.emptyTurtleSet
    else {
      val agentSetBuilder = new AgentSetBuilder(AgentKind.Turtle, patch.turtleCount)
      val it = patch.turtlesHere.iterator
      while(it.hasNext) {
        val turtle = it.next()
        if (turtle != null)
          agentSetBuilder.add(turtle)
      }
      agentSetBuilder.build()
    }
  }
}
