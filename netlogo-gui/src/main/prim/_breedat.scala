// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.AgentKind
import scala.collection.JavaConverters._

import org.nlogo.agent.{ AgentSet, Patch, Turtle }
import org.nlogo.api.{ AgentException}
import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _breedat(breedName: String) extends Reporter {


  override def toString: String = s"${super.toString}:$breedName"

  override def report(context: Context): AnyRef = {
    val dx = argEvalDoubleValue(context, 0)
    val dy = argEvalDoubleValue(context, 1)
    var patch: Patch = null
    try {
      patch = context.agent.getPatchAtOffsets(dx, dy)
    } catch {
      case e: AgentException =>
        return AgentSet.emptyTurtleSet
    }
    if (patch == null)
      return AgentSet.emptyTurtleSet
    val breed = world.getBreed(breedName)
    AgentSet.fromArray(
      AgentKind.Turtle,
      patch.turtlesHere.asScala.
        filter(turtle => turtle != null && (turtle.getBreed eq breed)).toArray)
  }
}
