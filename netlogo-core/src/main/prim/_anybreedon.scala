// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ AgentSet, Patch, Turtle }
import org.nlogo.core.{ AgentKind, I18N, Syntax }
import org.nlogo.nvm.{ ArgumentTypeException, Context, Reporter, RuntimePrimitiveException }

class _anybreedon(private[this] val _breedName: String) extends Reporter {

  def breedName = _breedName

  override def toString: String = s"${super.toString}:${_breedName}"

  override def report(context: Context): java.lang.Boolean =
    Boolean.box(report_1(context, args(0).report(context)))

  def report_1(context: Context, agentOrSet: AnyRef): Boolean = {
    val breed = world.getBreed(_breedName)
    agentOrSet match {
      case turtle: Turtle =>
        if(turtle.id == -1)
          throw new RuntimePrimitiveException(context, this, 
            I18N.errors.getN("org.nlogo.$common.thatAgentIsDead", turtle.classDisplayName))
        val itr = turtle.getPatchHere.turtlesHere.iterator
        while (itr.hasNext) {
          val t = itr.next()
          if (t.getBreed eq breed)
            return true
        }
        return false
      case patch: Patch =>
        val itr = patch.turtlesHere.iterator
        while (itr.hasNext) {
          val t = itr.next()
          if (t.getBreed eq breed)
            return true
        }
        return false
      case sourceSet: AgentSet =>
        if(sourceSet.kind == AgentKind.Turtle) {
          val sourceSetItr = sourceSet.iterator
          while (sourceSetItr.hasNext) {
            val turtleItr = sourceSetItr.next().asInstanceOf[Turtle].getPatchHere.turtlesHere.iterator
            while (turtleItr.hasNext) {
              val t = turtleItr.next()
              if (t.getBreed eq breed)
                return true
            }
            return false
          }
          return false
        } else if(sourceSet.kind == AgentKind.Patch) {
          val sourceSetItr = sourceSet.iterator
          while (sourceSetItr.hasNext) {
            val patchItr = sourceSetItr.next().asInstanceOf[Patch].turtlesHere.iterator
            while (patchItr.hasNext) {
              val t = patchItr.next()
              if (t.getBreed eq breed)
                return true
            }
            return false
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
