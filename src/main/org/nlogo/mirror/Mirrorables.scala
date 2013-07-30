// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import scala.language.implicitConversions
import org.nlogo.api.AgentVariableNumbers._
import org.nlogo.api
import org.nlogo.plot

object Mirrorables {

  case object Patch extends Kind
  case object Turtle extends Kind
  case object Link extends Kind
  case object Observer extends Kind
  case object World extends Kind
  case object WidgetValue extends Kind

  implicit def agentKindToMirrorKind(agentKind: api.AgentKind) = agentKind match {
    case api.AgentKind.Observer => Observer
    case api.AgentKind.Turtle   => Turtle
    case api.AgentKind.Patch    => Patch
    case api.AgentKind.Link     => Link
  }

  // so we don't fill up memory with duplicate AgentKey objects
  val keyCache = collection.mutable.WeakHashMap[api.Agent, AgentKey]()
  def getAgentKey(agent: api.Agent) = keyCache.getOrElseUpdate(agent, AgentKey(agent.kind, agent.id))
  abstract class MirrorableAgent[T <: api.Agent](agent: T) extends Mirrorable {
    override def getVariable(index: Int) = variables.getOrElse(index, agent.getVariable(index))
    override def agentKey = getAgentKey(agent)
  }

  object MirrorableTurtle {
    val tvLineThickness =
      api.AgentVariables.getImplicitTurtleVariables.size
    val lastIndex = tvLineThickness
  }
  class MirrorableTurtle(turtle: api.Turtle) extends MirrorableAgent(turtle) {
    import MirrorableTurtle._
    override def kind = Turtle
    override def nbVariables = lastIndex + 1
    override val variables = Map(
      VAR_BREED -> turtle.getBreed.printName,
      tvLineThickness -> Double.box(turtle.lineThickness))
  }

  class MirrorablePatch(patch: api.Patch) extends MirrorableAgent(patch) {
    override def kind = Patch
    override def nbVariables =
      api.AgentVariables.getImplicitPatchVariables.size
    override val variables = Map(
      VAR_PXCOR -> Int.box(patch.pxcor),
      VAR_PYCOR -> Int.box(patch.pycor))
  }

  object MirrorableLink {
    private val n = api.AgentVariables.getImplicitLinkVariables.size
    val Seq(
      lvSize,
      lvHeading,
      lvMidpointX,
      lvMidpointY,
      _*) = Stream.from(n)
    val lastIndex = lvMidpointY
  }
  class MirrorableLink(link: api.Link) extends MirrorableAgent(link) {
    import MirrorableLink._
    override def kind = Link
    override def nbVariables = lastIndex + 1
    override val variables = Map(
      VAR_END1 -> Long.box(link.end1.id),
      VAR_END2 -> Long.box(link.end2.id),
      VAR_LBREED -> link.getBreed.printName,
      lvSize -> Double.box(link.size),
      lvHeading -> Double.box(link.heading),
      lvMidpointX -> Double.box(link.midpointX),
      lvMidpointY -> Double.box(link.midpointY))
  }

  object MirrorableObserver {
    def ovTargetAgent(nbInterfaceGlobals: Int) = nbInterfaceGlobals
  }
  class MirrorableObserver(observer: api.Observer, nbInterfaceGlobals: Int) extends MirrorableAgent(observer) {
    import MirrorableObserver._
    override def kind = Observer
    override def nbVariables = nbInterfaceGlobals + 1
    private def targetAgent =
      Option(observer.targetAgent)
        .map(getAgentKey)
        .map(key => (Serializer.agentKindToInt(key.kind), key.id))
    override val variables = Map(
      ovTargetAgent(nbInterfaceGlobals) -> targetAgent)
  }

  object MirrorableWorld {
    object WorldVar extends Enumeration {
      type WorldVar = Value
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
      val NbInterfaceGlobals = Value("nbInterfaceGlobals")
    }
  }

  class MirrorableWorld(world: api.World) extends Mirrorable {
    import MirrorableWorld._
    import WorldVar._
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
      (UnbreededLinksAreDirected.id, Boolean.box(world.links.isDirected)),
      (NbInterfaceGlobals.id, Int.box(world.program.interfaceGlobals.size)))
  }

  object MirrorableWidgetValue {
    val wvvValueString = 0
  }

  class MirrorableWidgetValue(value: String, index: Int) extends Mirrorable {
    import MirrorableWidgetValue._
    def kind = WidgetValue
    def agentKey = AgentKey(kind, index)
    override val variables = Map(
      wvvValueString -> value)
  }

  def allMirrorables(world: api.World, widgetValues: Seq[(String, Int)] = Seq()): Iterable[Mirrorable] = {
    import collection.JavaConverters._
    val turtles = world.turtles.agents.asScala.map(t => new MirrorableTurtle(t.asInstanceOf[api.Turtle]))
    val patches = world.patches.agents.asScala.map(p => new MirrorablePatch(p.asInstanceOf[api.Patch]))
    val links = world.links.agents.asScala.map(l => new MirrorableLink(l.asInstanceOf[api.Link]))
    val worldIterable = Iterable(new MirrorableWorld(world))
    val observerIterable = Iterable(new MirrorableObserver(world.observer, world.program.interfaceGlobals.size))
    val widgetValuesIterable = widgetValues.map { case (v, i) => new MirrorableWidgetValue(v, i) }
    (worldIterable ++ observerIterable ++ widgetValuesIterable ++ turtles ++ patches ++ links)
  }

}

