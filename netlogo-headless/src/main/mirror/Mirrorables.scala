// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import scala.language.implicitConversions
import org.nlogo.{ api, core }
import org.nlogo.api.AgentVariableNumbers._
import org.nlogo.core.AgentVariables.implicitLinkVariables
import org.nlogo.core.AgentVariables.implicitPatchVariables
import org.nlogo.core.AgentVariables.implicitTurtleVariables

object Mirrorables {

  case object Patch extends Kind {
    object PatchVariables extends Enumeration {
      implicitPatchVariables.foreach(Value)
    }
    val Variables = PatchVariables
  }
  case object Turtle extends Kind {
    object TurtleVariables extends Enumeration {
      implicitTurtleVariables.foreach(Value)
      val LineThickness = Value("lineThickness")
    }
    val Variables = TurtleVariables
  }
  case object Link extends Kind {
    object LinkVariables extends Enumeration {
      implicitLinkVariables.foreach(Value)
      val Size = Value("size")
      val Heading = Value("heading")
      val MidpointX = Value("midpointX")
      val MidpointY = Value("midpointY")
      val IsDirected = Value("isDirected")
    }
    val Variables = LinkVariables
  }
  case object Observer extends Kind {
    object ObserverVariables extends Enumeration {
      val TargetAgent = Value("targetAgent")
      val Perspective = Value("perspective")
    }
    val Variables = ObserverVariables
  }
  case object World extends Kind {
    object WorldVariables extends Enumeration {
      val Ticks = Value("ticks")
      val PatchesWithLabels = Value("patchesWithLabels")
      val TurtleShapeList = Value("turtleShapeList")
      val LinkShapeList = Value("linkShapeList")
      val PatchSize = Value("patchSize")
      val WorldWidth = Value("worldWidth")
      val WorldHeight = Value("worldHeight")
      val MinPxcor = Value("minPxcor")
      val MinPycor = Value("minPycor")
      val MaxPxcor = Value("maxPxcor")
      val MaxPycor = Value("maxPycor")
      val WrappingAllowedInX = Value("wrappingAllowedInX")
      val WrappingAllowedInY = Value("wrappingAllowedInY")
      val PatchesAllBlack = Value("patchesAllBlack")
      val TurtleBreeds = Value("turtleBreeds")
      val LinkBreeds = Value("linkBreeds")
      val UnbreededLinksAreDirected = Value("unbreededLinksAreDirected")
    }
    val Variables = WorldVariables
  }
  case object WidgetValue extends Kind {
    object WidgetVariables extends Enumeration {
      val ValueString = Value("valueString")
    }
    val Variables = WidgetVariables
  }

  implicit def agentKindToMirrorKind(agentKind: core.AgentKind) = agentKind match {
    case core.AgentKind.Observer => Observer
    case core.AgentKind.Turtle => Turtle
    case core.AgentKind.Patch => Patch
    case core.AgentKind.Link => Link
  }

  abstract class MirrorableAgent[T <: api.Agent](agent: T) extends Mirrorable {
    override def getVariable(index: Int) = variables.getOrElse(index, agent.getVariable(index))
    override val agentKey = AgentKey(agent.kind, agent.id)
  }

  object MirrorableTurtle {
    val tvLineThickness = implicitTurtleVariables.size
    val lastIndex = tvLineThickness
  }
  class MirrorableTurtle(turtle: api.Turtle) extends MirrorableAgent(turtle) {
    import Turtle.Variables._
    override def kind = Turtle
    override val variables = Map(
      VAR_BREED -> turtle.getBreed.printName,
      VAR_LABEL -> turtle.labelString,
      LineThickness.id -> Double.box(turtle.lineThickness))
  }

  class MirrorablePatch(patch: api.Patch) extends MirrorableAgent(patch) {
    override def kind = Patch
    override val variables = Map(
      VAR_PXCOR -> Int.box(patch.pxcor),
      VAR_PYCOR -> Int.box(patch.pycor),
      VAR_PLABEL -> patch.labelString)
  }

  class MirrorableLink(link: api.Link) extends MirrorableAgent(link) {
    import Link.Variables._
    override def kind = Link
    override val variables = Map(
      VAR_END1 -> Long.box(link.end1.id),
      VAR_END2 -> Long.box(link.end2.id),
      VAR_LBREED -> link.getBreed.printName,
      VAR_LLABEL -> link.labelString,
      Size.id -> Double.box(link.size),
      Heading.id -> Double.box(link.heading),
      MidpointX.id -> Double.box(link.midpointX),
      MidpointY.id -> Double.box(link.midpointY),
      IsDirected.id -> Boolean.box(link.isDirectedLink))
  }

  class MirrorableObserver(observer: api.Observer) extends MirrorableAgent(observer) {
    import Observer.Variables._
    override def kind = Observer
    private def targetAgent =
      Option(observer.targetAgent)
        .map(agent => (Serializer.agentKindToInt(agent.kind), agent.id))
    override val variables = Map(
      TargetAgent.id -> targetAgent,
      Perspective.id -> Int.box(observer.perspective.export))
  }

  class MirrorableWorld(world: api.World) extends Mirrorable {
    import World.Variables._
    override def kind = World
    override def agentKey = AgentKey(kind, 0) // dummy id for the one and unique world
    // pending resolution of https://issues.scala-lang.org/browse/SI-6723
    // we avoid the `a -> b` syntax in favor of `(a, b)` - ST 1/9/13
    override val variables = Map(
      (Ticks.id, Double.box(world.ticks)),
      (PatchesWithLabels.id, Int.box(world.patchesWithLabels)),
      (TurtleShapeList.id, world.turtleShapeList), // probably not good enough to just pass the shapelists like that..
      (LinkShapeList.id, world.linkShapeList),
      (PatchSize.id, Double.box(world.patchSize)),
      (WorldWidth.id, Int.box(world.worldWidth)),
      (WorldHeight.id, Int.box(world.worldHeight)),
      (MinPxcor.id, Int.box(world.minPxcor)),
      (MinPycor.id, Int.box(world.minPycor)),
      (MaxPxcor.id, Int.box(world.maxPxcor)),
      (MaxPycor.id, Int.box(world.maxPycor)),
      (WrappingAllowedInX.id, Boolean.box(world.wrappingAllowedInX)),
      (WrappingAllowedInY.id, Boolean.box(world.wrappingAllowedInY)),
      (PatchesAllBlack.id, Boolean.box(world.patchesAllBlack)),
      (TurtleBreeds.id, world.program.breeds.map { case (breedName, breed) => breedName -> breed.isDirected }),
      (LinkBreeds.id, world.program.linkBreeds.map { case (breedName, breed) => breedName -> breed.isDirected }),
      (UnbreededLinksAreDirected.id, Boolean.box(world.links.isDirected))
    )
  }

  def allMirrorables(world: api.World): Iterable[Mirrorable] = {
    import collection.JavaConverters._
    val turtles = world.turtles.agents.asScala.map(t => new MirrorableTurtle(t.asInstanceOf[api.Turtle]))
    val patches = world.patches.agents.asScala.map(p => new MirrorablePatch(p.asInstanceOf[api.Patch]))
    val links = world.links.agents.asScala.map(l => new MirrorableLink(l.asInstanceOf[api.Link]))
    val worldIterable = Iterable(new MirrorableWorld(world))
    val observerIterable = Iterable(new MirrorableObserver(world.observer))
    (worldIterable ++ observerIterable ++ turtles ++ patches ++ links)
  }
}
