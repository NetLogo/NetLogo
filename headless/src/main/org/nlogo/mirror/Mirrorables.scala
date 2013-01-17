// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import scala.language.implicitConversions
import org.nlogo.api.AgentVariableNumbers._
import org.nlogo.api
import org.nlogo.plot
import collection.JavaConverters._

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
    val Seq(
      wvTicks,
      wvPatchesWithLabels,
      wvTurtleShapeList,
      wvLinkShapeList,
      wvPatchSize,
      wvWorldWidth,
      wvWorldHeight,
      wvMinPxcor,
      wvMinPycor,
      wvMaxPxcor,
      wvMaxPycor,
      wvWrappingAllowedInX,
      wvWrappingAllowedInY,
      wvPatchesAllBlack,
      wvTurtleBreeds,
      wvLinkBreeds,
      wvUnbreededLinksAreDirected,
      wvTrailDrawing,
      wvNbInterfaceGlobals,
      _*) = Stream.from(0)
  }

  class MirrorableWorld(world: api.World) extends Mirrorable {
    import MirrorableWorld._
    override def kind = World
    override def agentKey = AgentKey(kind, 0) // dummy id for the one and unique world
    // pending resolution of https://issues.scala-lang.org/browse/SI-6723
    // we avoid the `a -> b` syntax in favor of `(a, b)` - ST 1/9/13
    override val variables = Map(
      (wvTicks, Double.box(world.ticks)),
      (wvPatchesWithLabels, Int.box(world.patchesWithLabels)),
      (wvTurtleShapeList, world.turtleShapeList), // probably not good enough to just pass the shapelists like that..
      (wvLinkShapeList, world.linkShapeList),
      (wvPatchSize, Double.box(world.patchSize)),
      (wvWorldWidth, Int.box(world.worldWidth)),
      (wvWorldHeight, Int.box(world.worldHeight)),
      (wvMinPxcor, Int.box(world.minPxcor)),
      (wvMinPycor, Int.box(world.minPycor)),
      (wvMaxPxcor, Int.box(world.maxPxcor)),
      (wvMaxPycor, Int.box(world.maxPycor)),
      (wvWrappingAllowedInX, Boolean.box(world.wrappingAllowedInX)),
      (wvWrappingAllowedInY, Boolean.box(world.wrappingAllowedInY)),
      (wvPatchesAllBlack, Boolean.box(world.patchesAllBlack)),
      (wvTurtleBreeds, world.program.breeds.map { case (breedName, breed) => breedName -> breed.isDirected }),
      (wvLinkBreeds, world.program.linkBreeds.map { case (breedName, breed) => breedName -> breed.isDirected }),
      (wvUnbreededLinksAreDirected, Boolean.box(world.links.isDirected)),
      (wvTrailDrawing,
        (if (world.trailDrawer.isDirty) {
          val outputStream = new java.io.ByteArrayOutputStream
          val img = world.trailDrawer.getDrawing.asInstanceOf[java.awt.image.BufferedImage]
          javax.imageio.ImageIO.write(img, "png", outputStream)
          Some(outputStream.toByteArray())
        } else None)),
      (wvNbInterfaceGlobals, Int.box(world.program.interfaceGlobals.size)))
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

  def allMirrorables(world: api.World, widgetValues: Seq[(String, Int)]): Iterable[Mirrorable] = {
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

