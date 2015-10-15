// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import scala.collection.JavaConverters._

import org.nlogo.agent.{ ArrayAgentSet, Patch, Turtle }
import org.nlogo.api.{ AgentException, Syntax }
import org.nlogo.nvm.{ Context, Reporter }

class _breedat(breedName: String) extends Reporter {
  override def syntax: org.nlogo.core.Syntax =
    Syntax.reporterSyntax(
      Array[Int](Syntax.NumberType, Syntax.NumberType),
      Syntax.TurtlesetType,
      "-TP-")

  override def toString: String = s"${super.toString}:$breedName"

  override def report(context: Context): AnyRef = {
    val dx = argEvalDoubleValue(context, 0)
    val dy = argEvalDoubleValue(context, 1)
    var patch: Patch = null
    try {
      patch = context.agent.getPatchAtOffsets(dx, dy)
    } catch {
      case e: AgentException =>
        return new ArrayAgentSet(classOf[Turtle], 0, false, world)
    }
    if (patch == null)
      return new ArrayAgentSet(classOf[Turtle], 0, false, world)
    val breed = world.getBreed(breedName)
    new ArrayAgentSet(
      classOf[Turtle],
      patch.turtlesHere.asScala.
        filter(turtle => turtle != null && turtle.getBreed == breed).toArray,
      world)
  }
}
