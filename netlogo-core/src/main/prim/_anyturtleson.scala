// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ AgentSet, Patch, Turtle }
import org.nlogo.core.{ AgentKind, I18N, Syntax }
import org.nlogo.nvm.{ ArgumentTypeException, Context, Reporter, RuntimePrimitiveException }

class _anyturtleson extends Reporter {
  override def report(context: Context): java.lang.Boolean =
    Boolean.box(report_1(context, args(0).report(context)))

  def report_1(context: Context, agentOrSet: AnyRef): Boolean = {
    agentOrSet match {
      case turtle: Turtle =>
        if(turtle.id == -1)
          throw new RuntimePrimitiveException(context, this, 
            I18N.errors.getN("org.nlogo.$common.thatAgentIsDead", turtle.classDisplayName))
        return true
      case patch: Patch =>
        val itr = patch.turtlesHere.iterator
        return itr.hasNext
      case sourceSet: AgentSet =>
        if(sourceSet.kind == AgentKind.Turtle) {
          val sourceSetItr = sourceSet.iterator
          return sourceSetItr.hasNext
        } else if(sourceSet.kind == AgentKind.Patch) {
          val sourceSetItr = sourceSet.iterator
          while (sourceSetItr.hasNext) {
            val patchItr = sourceSetItr.next().asInstanceOf[Patch].turtlesHere.iterator
            if(patchItr.hasNext)
              return true
          }
          false
        }
        false
      case _ =>
        throw new ArgumentTypeException(
          context, this, 0,
          Syntax.TurtleType | Syntax.PatchType |
          Syntax.TurtlesetType | Syntax.PatchsetType,
          agentOrSet)
    }
  }
}
