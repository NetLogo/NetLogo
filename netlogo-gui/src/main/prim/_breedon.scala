// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.{AgentKind, I18N}

import scala.collection.mutable
import org.nlogo.agent.{Agent, AgentSet, AgentSetBuilder, ArrayAgentSet, Patch, Turtle}
import org.nlogo.core.Syntax
import org.nlogo.nvm.{ArgumentTypeException, Context, Reporter}
import org.nlogo.nvm.RuntimePrimitiveException

class _breedon(breedName: String) extends Reporter {


  override def toString: String = s"${super.toString}:$breedName"

  override def report(context: Context) = report_1(context, args(0).report(context))

  def report_1(context: Context, agentOrSet: AnyRef): AgentSet = {
    val agentSetBuilder = new AgentSetBuilder(AgentKind.Turtle)
    val breed = world.getBreed(breedName)
    agentOrSet match {
      case turtle: Turtle =>
        if (turtle.id == -1)
          throw new RuntimePrimitiveException(context, this,
            I18N.errors.getN("org.nlogo.$common.thatAgentIsDead", turtle.classDisplayName))
        val itr = turtle.getPatchHere.turtlesHere.iterator
        while (itr.hasNext) {
          val t = itr.next()
          if (t.getBreed eq breed)
            agentSetBuilder.add(t)
        }
      case patch: Patch =>
        val itr = patch.turtlesHere.iterator
        while (itr.hasNext) {
          val t = itr.next()
          if (t.getBreed eq breed)
            agentSetBuilder.add(t)
        }
      case sourceSet: AgentSet =>
        if (sourceSet.kind == AgentKind.Turtle) {
          val sourceSetItr = sourceSet.iterator
          while (sourceSetItr.hasNext) {
            val turtleItr =
              sourceSetItr.next().asInstanceOf[Turtle].getPatchHere.turtlesHere.iterator
            while (turtleItr.hasNext) { val t = turtleItr.next()
              if (t.getBreed eq breed)
                agentSetBuilder.add(t)
            }
          }
        } else if (sourceSet.kind == AgentKind.Patch) {
          val sourceSetItr = sourceSet.iterator
          while (sourceSetItr.hasNext) {
            val patchItr = sourceSetItr.next().asInstanceOf[Patch].turtlesHere.iterator
            while (patchItr.hasNext) {
              val t = patchItr.next()
              if (t.getBreed eq breed)
                agentSetBuilder.add(t)
            }
          }
        }
      case _ =>
        throw new ArgumentTypeException(
          context, this, 0,
          Syntax.TurtleType | Syntax.PatchType |
          Syntax.TurtlesetType | Syntax.PatchsetType,
          agentOrSet)
    }
    agentSetBuilder.build()
  }

  def report_2(context: Context, sourceSet: AgentSet): AgentSet = {
    val agentSetBuilder = new AgentSetBuilder(AgentKind.Turtle)
    val breed = world.getBreed(breedName)
    if (sourceSet.kind == AgentKind.Turtle) {
      val sourceSetItr = sourceSet.iterator
      while (sourceSetItr.hasNext) {
        val turtleItr =
          sourceSetItr.next().asInstanceOf[Turtle].getPatchHere.turtlesHere.iterator
        while (turtleItr.hasNext) {
          val t = turtleItr.next()
          if (t.getBreed eq breed)
            agentSetBuilder.add(t)
        }
      }
    } else if (sourceSet.kind == AgentKind.Patch) {
      val sourceSetItr = sourceSet.iterator
      while (sourceSetItr.hasNext) {
        val patchItr = sourceSetItr.next().asInstanceOf[Patch].turtlesHere.iterator
        while (patchItr.hasNext) {
          val t = patchItr.next()
          if (t.getBreed eq breed)
            agentSetBuilder.add(t)
        }
      }
    } else {
      throw new ArgumentTypeException(
        context, this, 0,
        Syntax.TurtleType | Syntax.PatchType |
        Syntax.TurtlesetType | Syntax.PatchsetType,
        sourceSet)
    }
    agentSetBuilder.build()
  }

  def report_3(context: Context, agent: Agent): AgentSet = {
    val agentSetBuilder = new AgentSetBuilder(AgentKind.Turtle)
    val breed = world.getBreed(breedName)
    agent match {
      case turtle: Turtle =>
        if (turtle.id == -1)
          throw new RuntimePrimitiveException(context, this,
            I18N.errors.getN("org.nlogo.$common.thatAgentIsDead", turtle.classDisplayName))
        val itr = turtle.getPatchHere.turtlesHere.iterator
        while (itr.hasNext) {
          val t = itr.next()
          if (t.getBreed eq breed)
            agentSetBuilder.add(t)
        }
      case patch: Patch =>
        val itr = patch.turtlesHere.iterator
        while (itr.hasNext) {
          val t = itr.next()
          if (t.getBreed eq breed)
            agentSetBuilder.add(t)
        }
      case _ =>
        throw new ArgumentTypeException(
          context, this, 0,
          Syntax.TurtleType | Syntax.PatchType |
          Syntax.TurtlesetType | Syntax.PatchsetType,
          agent)
    }
    agentSetBuilder.build()
  }

  def report_4(context: Context, turtle: Turtle): AgentSet = {
    val agentSetBuilder = new AgentSetBuilder(AgentKind.Turtle)
    val breed = world.getBreed(breedName)
    if (turtle.id == -1)
      throw new RuntimePrimitiveException(context, this,
        I18N.errors.getN("org.nlogo.$common.thatAgentIsDead", turtle.classDisplayName))
    val itr = turtle.getPatchHere.turtlesHere.iterator
    while (itr.hasNext) {
      val t = itr.next()
      if (t.getBreed eq breed)
        agentSetBuilder.add(t)
    }
    agentSetBuilder.build()
  }

  def report_5(context: Context, patch: Patch): AgentSet = {
    val agentSetBuilder = new AgentSetBuilder(AgentKind.Turtle)
    val breed = world.getBreed(breedName)
    val itr = patch.turtlesHere.iterator
    while (itr.hasNext) {
      val t = itr.next()
      if (t.getBreed eq breed)
        agentSetBuilder.add(t)
    }
    agentSetBuilder.build()
  }
}
