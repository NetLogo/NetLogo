package org.nlogo.prim.threed

import org.nlogo.agent.{ Agent3D, ArrayAgentSet, Turtle }
import org.nlogo.api.AgentException
import org.nlogo.nvm.{ Context, Reporter, Syntax }

class _turtlesat extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.TYPE_NUMBER,
                                Syntax.TYPE_NUMBER,
                                Syntax.TYPE_NUMBER),
                          Syntax.TYPE_TURTLESET, "-TP-")
  override def report(context: Context): ArrayAgentSet = {
    val dx = argEvalDoubleValue(context, 0)
    val dy = argEvalDoubleValue(context, 1)
    val dz = argEvalDoubleValue(context, 2)
    val patch = 
      try context.agent.asInstanceOf[Agent3D].getPatchAtOffsets(dx, dy, dz)
      catch {
        case _: AgentException =>
          return new ArrayAgentSet(classOf[Turtle], 0, false, world)
      }
    if (patch == null)
      new ArrayAgentSet(classOf[Turtle], 0, false, world)
    else {
      val agentset = new ArrayAgentSet(
        classOf[Turtle], patch.turtleCount, false, world)
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
